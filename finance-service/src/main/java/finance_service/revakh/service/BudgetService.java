package finance_service.revakh.service;

import finance_service.revakh.DTO.ConsumptionDTO;
import finance_service.revakh.DTO.BudgetRequestDTO;
import finance_service.revakh.DTO.BudgetUpdateDTO;
import finance_service.revakh.events.BudgetCreatedEvent;
import finance_service.revakh.events.BudgetUpdatedEvent;
import finance_service.revakh.exceptions.BudgetExceptions.BudgetAlreadyExistsException;
import finance_service.revakh.exceptions.BudgetExceptions.BudgetNotFoundException;
import finance_service.revakh.exceptions.BudgetExceptions.BudgetValidationException;
import finance_service.revakh.messages.BudgetEventPublisher;
import finance_service.revakh.models.*;
import finance_service.revakh.repo.BudgetRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

//budget service is also done
@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {
    final private BudgetRepo budgetRepo;

    final private WalletService walletService;
    final private FinanceUserService financeUserService;
    final private TransactionLedgerService transactionLedgerService;
    final private CategoryService categoryService;
    final private BudgetEventPublisher budgetEventPublisher;

    //create budget
    public Budget createBudget(BudgetRequestDTO dto){

        FinanceUser financeUser =financeUserService.getUser(dto.getFinanceUserId());
        Category category = categoryService.getCategoryByIdAndUser(dto.getCategoryId(),financeUser);

        if (!category.getFinanceUser().equals(financeUser)) {
            throw new BudgetValidationException("Category Does Not Belong To This User");
        }

        if (category.getType() == CategoryType.INCOME) {
            throw new BudgetValidationException("Budgets Are Only Allowed For EXPENSE Categories");
        }

        if (dto.getLimitAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BudgetValidationException("Budget Cannot be 0");
        }

        if (dto.isActive()) {

            if (budgetRepo.existsActiveBudget(financeUser, category, dto.getPeriod())) {
                throw new BudgetAlreadyExistsException("Active Budget Exists For This Category & Period");
            }
        }

        Budget budget = Budget.builder()
                .financeUser(financeUser)
                .category(category)
                .isActive(dto.isActive())
                .limitAmount(dto.getLimitAmount())
                .period(dto.getPeriod())
                .periodStart(computeStart(dto.getPeriod()))
                .periodEnd(computeEnd(dto.getPeriod()))
                .build();
        Budget createdBudget = budgetRepo.save(budget);
        budgetEventPublisher.budgetCreatedPublisher(mapToCreateEvent(createdBudget));

        return createdBudget;
    }

    private LocalDate computeStart(Period p) {
        LocalDate today = LocalDate.now();
        return switch (p) {
            case DAILY -> today;
            case WEEKLY -> today.minusDays(today.getDayOfWeek().getValue() - 1);
            case MONTHLY -> today.withDayOfMonth(1);
            case YEARLY -> today.withDayOfYear(1);
        };
    }

    private LocalDate computeEnd(Period p) {
        LocalDate today = LocalDate.now();
        return switch (p) {
            case DAILY -> today;
            case WEEKLY -> computeStart(Period.WEEKLY).plusDays(6);
            case MONTHLY -> today.withDayOfMonth(today.lengthOfMonth());
            case YEARLY -> today.withDayOfYear(today.lengthOfYear());
        };

    }
    //delete one budget
    @Transactional
    public void deleteOneBudget(Long id){
        Budget budget = budgetRepo.findById(id).orElseThrow(()-> new BudgetNotFoundException("Budget Not Found"));
        budget.setActive(false);
        Budget savedBudget = budgetRepo.save(budget);

        //we need to publish that the budget has been made inactive
        budgetEventPublisher.budgetUpdatedPublisher(mapToUpdateEvent(savedBudget));
    }
    //delete all budgets under the username
    @Transactional
    public void deleteAllBudgetForTheUser(FinanceUser user){
                List<Budget> list = budgetRepo.findAllByFinanceUserAndIsActiveTrue(user).orElseThrow(()-> new BudgetNotFoundException("Budget Not Found"));
        for(Budget b : list){
            b.setActive(false);
        }
        List<Budget> savedList = budgetRepo.saveAll(list);

        // we need to publish for each budget that has been made inactive
        for(Budget b : savedList) {
            budgetEventPublisher.budgetUpdatedPublisher(mapToUpdateEvent(b));
        }
    }

    @Transactional
    public void deactivateBudgetsByCategory(Category category) {
        // 1. Find all budgets tied to this category that are still active
        List<Budget> activeBudgets = budgetRepo.findAllByCategoryAndIsActiveTrue(category);

        if (activeBudgets.isEmpty()) {
            return; // Nothing to do
        }

        // 2. Mark them as inactive
        activeBudgets.forEach(budget -> {
            budget.setActive(false);

            // 3. Publish the update event so other services stay in sync
            budgetEventPublisher.budgetUpdatedPublisher(mapToUpdateEvent(budget));
        });

        // 4. Save the batch update
        budgetRepo.saveAll(activeBudgets);

        log.info("Deactivated {} active budgets for Category: {}",
                activeBudgets.size(), category.getName());
    }

    //update budget
    @Transactional
    public Budget updateBudget(Long id, BudgetUpdateDTO dto) {

        Budget old = budgetRepo.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException("Budget Not Found"));

        boolean periodChanged = !old.getPeriod().equals(dto.getPeriod());
        boolean limitChanged = dto.getLimitAmount() != null &&
                old.getLimitAmount().compareTo(dto.getLimitAmount()) != 0;
        boolean activeChanged = old.isActive() != dto.isActive();

        // (If user deactivates budget, we don't need to enforce "totalBudget <= wallet")

        // CASE A: Simple edits (limit or active only)
        if (!periodChanged) {
            if (limitChanged) {
                old.setLimitAmount(dto.getLimitAmount());
            }
            if (activeChanged) {
                old.setActive(dto.isActive());
            }
            Budget savedBudget = budgetRepo.save(old);
            //as the limit is changed we must publish the budget update event
            budgetEventPublisher.budgetUpdatedPublisher(mapToUpdateEvent(savedBudget));

            return savedBudget;
        }

        // CASE B: Period changed -> close old + create new
        old.setActive(false);
        Budget savedBudget = budgetRepo.save(old);
        //the old budget is now set inactive
        budgetEventPublisher.budgetUpdatedPublisher(mapToUpdateEvent(savedBudget));

        Period newPeriod = dto.getPeriod();

        Budget newBudget = Budget.builder()
                .financeUser(old.getFinanceUser())
                .category(old.getCategory())
                .limitAmount(dto.getLimitAmount())
                .period(newPeriod)
                .periodStart(computeStart(newPeriod))
                .periodEnd(computeEnd(newPeriod))
                .isActive(true)
                .build();
        Budget savedNew = budgetRepo.save(newBudget);

        //new budget with the updated period, but its a new budget
        budgetEventPublisher.budgetCreatedPublisher(mapToCreateEvent(savedNew));
        return savedNew;
    }

    //get user budgets
    public List<Budget> getUserBudgets(Long userId){
        FinanceUser financeUser =financeUserService.getUser(userId);
        return budgetRepo.findActiveBudgetsSorted(financeUser);
    }

    // compute budget consumption
    public ConsumptionDTO computeConsumption(Long budgetId) {

        Budget budget = budgetRepo.findById(budgetId)
                .orElseThrow(() -> new BudgetNotFoundException("Budget Not Found"));

        FinanceUser user = budget.getFinanceUser();
        Category category = budget.getCategory();
        if (category != null && !category.getFinanceUser().equals(user)) {
            throw new BudgetValidationException("Category Does Not Belong To This User");
        }


        BigDecimal spent = transactionLedgerService.computeSpent(
                user,
                category,
                budget.getPeriodStart(),
                budget.getPeriodEnd()
        );

        BigDecimal limit = budget.getLimitAmount();
        BigDecimal remaining = limit.subtract(spent).max(BigDecimal.ZERO);

        int percentage = spent
                .multiply(BigDecimal.valueOf(100))
                .divide(limit, 0, RoundingMode.DOWN)
                .intValue();
        if (limit.compareTo(BigDecimal.ZERO) == 0) {
            percentage = 0;
        }
        BigDecimal overspent = BigDecimal.ZERO;
        if (spent.compareTo(limit) > 0) {
            overspent = spent.subtract(limit);
        }
        return ConsumptionDTO.builder()
                .budgetId(budget.getBudgetId())
                .limitAmount(limit)
                .spentAmount(spent)
                .remainingAmount(remaining)
                .percentageUsed(percentage)
                .overspent(overspent)
                .isHalfReached(percentage >= 50)
                .isEightyReached(percentage>=80)
                .isFullReached(percentage>=100)
                .budgetExceeded(overspent.compareTo(BigDecimal.ZERO)>0)
                .build();
    }

    //trying to check if period expires everyday
    @Scheduled(cron = "0 1 0 * * *")//cron job starts everyday
    @Transactional
    public void resetExpiredBudgets() {
        List<Budget> budgets = budgetRepo.findAllByIsActiveTrue();

        LocalDate today = LocalDate.now();

        //basicaly if for this current budget today>endDate then reset its window
        for (Budget budget : budgets) {
            if (today.isAfter(budget.getPeriodEnd())) {
                log.info("Reset triggered for Budget {} belonging to User {}",
                        budget.getBudgetId(),
                        budget.getFinanceUser().getUserId());

                resetPeriod(budget);
            }
        }
    }

    //resetting the period
    private void resetPeriod(Budget budget) {
        LocalDate start = budget.getPeriodStart();
        LocalDate end = budget.getPeriodEnd();
        LocalDate today = LocalDate.now();

        // Keep updating until current date is INSIDE the period
        while (today.isAfter(end)) {
            switch (budget.getPeriod()) {
                case DAILY -> {
                    start = start.plusDays(1);
                    end = end.plusDays(1);
                }
                case WEEKLY -> {
                    start = start.plusWeeks(1);
                    end = end.plusWeeks(1);
                }
                case MONTHLY -> {
                    // This ensures "Jan 31" + 1 month becomes "Feb 1", not "Feb 28".
                    start = start.plusMonths(1).withDayOfMonth(1);

                    // End date is simply the last day of that new month
                    end = start.withDayOfMonth(start.lengthOfMonth());
                }
                case YEARLY -> {
                    // FIX: Force Start to Jan 1st of the new year
                    start = start.plusYears(1).withDayOfYear(1);

                    // End is Dec 31st of that new year
                    end = start.withDayOfYear(start.lengthOfYear());
                }
            }
        }

        budget.setPeriodStart(start);
        budget.setPeriodEnd(end);

        budgetRepo.save(budget);
        log.info("Budget {} reset SUCCESS. New Cycle: {} to {}",
                budget.getBudgetId(), start, end);
    }

    //totalBUdget is the sum of all active budgets it is not equivalent to global budget

    //global budget can be set by the user explicitely
    public BigDecimal totalBudget(Long userId){
        BigDecimal total = BigDecimal.ZERO;
        FinanceUser user = financeUserService.getUser(userId);
        List<Budget> list = budgetRepo.findActiveBudgetsSorted(user);
        for(Budget b : list){
            total = total.add(b.getLimitAmount());
        }
        return total;
    }

    public boolean categoryHasActiveBudgets(Category category){
        return budgetRepo.existsByCategoryAndIsActiveTrue(category);
    }


    //send notifications on 50%, 80% and 100% budget consumption


    private BudgetUpdatedEvent mapToUpdateEvent(Budget budget) {
        return BudgetUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(budget.getFinanceUser().getUserId()) // Adjust based on your User entity
                .budgetId(budget.getBudgetId())
                .category(budget.getCategory().getName())
                .newLimitAmount(budget.getLimitAmount())
                .period(budget.getPeriod().toString())
                .startDate(budget.getPeriodStart())
                .endDate(budget.getPeriodEnd())
                .active(budget.isActive())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private BudgetCreatedEvent mapToCreateEvent(Budget budget) {
        return BudgetCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(budget.getFinanceUser().getUserId())
                .budgetId(budget.getBudgetId())
                .category(budget.getCategory().getName())
                .limitAmount(budget.getLimitAmount())
                .period(budget.getPeriod().toString())
                .startDate(budget.getPeriodStart())
                .endDate(budget.getPeriodEnd())
                .active(budget.isActive())
                .createdAt(LocalDateTime.now())
                .build();
    }

}
