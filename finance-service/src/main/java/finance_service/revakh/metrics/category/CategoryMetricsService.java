package finance_service.revakh.metrics.category;

import finance_service.revakh.metrics.PeriodPreset;
import finance_service.revakh.metrics.category.DTO.CategoryBreakdownDTO;
import finance_service.revakh.models.*;
import finance_service.revakh.repo.BudgetRepo;
import finance_service.revakh.repo.CategoryRepo;
import finance_service.revakh.repo.TransactionLedgerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryMetricsService {

    private final TransactionLedgerRepo transactionLedgerRepo;
    private final CategoryRepo categoryRepo;
    private final BudgetRepo budgetRepo;

    /**
     * Main entry point to get category health for a specific time range.
     */
    public List<CategoryBreakdownDTO> getCategoryBreakdown(PeriodPreset preset, Long userId) {

        // --- STEP 1: RESOLVE TIME PERIODS ---
        LocalDate[] currentDates = resolveDates(preset);
        LocalDate[] previousDates = getPreviousPeriodDates(currentDates[0], currentDates[1]);

        // --- STEP 2: FETCH TRANSACTIONS EFFICIENTLY ---
        // Fetch Current Transactions
        List<TransactionLedger> currentTx = transactionLedgerRepo.findByUserIdAndTransactionTypeAndDateBetween(
                userId, TransactionType.DEBIT, currentDates[0], currentDates[1]);

        // Fetch Previous Transactions (for Trend analysis)
        List<TransactionLedger> previousTx = transactionLedgerRepo.findByUserIdAndTransactionTypeAndDateBetween(
                userId, TransactionType.DEBIT, previousDates[0], previousDates[1]);

        // --- STEP 3: PRE-CALCULATE TOTALS ---
        BigDecimal totalSpentGlobal = currentTx.stream()
                .map(TransactionLedger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // --- STEP 4: GROUP DATA IN MEMORY ---
        // Group Current Transactions by Category ID
        Map<Long, List<TransactionLedger>> currentMap = currentTx.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getCategoryId()));

        // Group Previous Transactions by Category ID (Sum only)
        Map<Long, BigDecimal> previousMap = previousTx.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getCategoryId(),
                        Collectors.reducing(BigDecimal.ZERO, TransactionLedger::getAmount, BigDecimal::add)
                ));

        // --- STEP 5: FETCH BUDGETS EFFICIENTLY (PERFORMANCE FIX) ---
        // Fetch ALL budgets for this user once.
        // NOTE: Ensure your BudgetRepo has: List<Budget> findAllByFinanceUser_UserId(Long userId);
        List<Budget> userBudgets = budgetRepo.findAllByFinanceUser_UserId(userId);

        // Map Budgets by Category ID for instant lookup
        Map<Long, Budget> budgetMap = userBudgets.stream()
                .collect(Collectors.toMap(
                        b -> b.getCategory().getCategoryId(),
                        b -> b,
                        (existing, replacement) -> existing // Safety strategy for duplicates
                ));

        // --- STEP 6: BUILD THE METRICS ---
        List<Category> allCategories = categoryRepo.findAllByUserId(userId);
        List<CategoryBreakdownDTO> dtos = new ArrayList<>();

        for (Category cat : allCategories) {
            // 1. Get Transactions (Fast Memory Lookup)
            List<TransactionLedger> catTx = currentMap.getOrDefault(cat.getCategoryId(), new ArrayList<>());

            // 2. Calculate Totals
            BigDecimal currentSpent = catTx.stream()
                    .map(TransactionLedger::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal prevSpent = previousMap.getOrDefault(cat.getCategoryId(), BigDecimal.ZERO);

            // 3. Get Budget (Fast Memory Lookup)
            Budget budget = budgetMap.get(cat.getCategoryId());

            // 4. Budget Logic
            BigDecimal limit = (budget != null) ? budget.getLimitAmount() : BigDecimal.ZERO;
            boolean hasBudget = limit.compareTo(BigDecimal.ZERO) > 0;

            // 5. Build DTO
            dtos.add(CategoryBreakdownDTO.builder()
                    .categoryId(cat.getCategoryId())
                    .categoryName(cat.getName())

                    // Spending Metrics
                    .totalSpentInCategory(currentSpent)
                    .totalTransactionsInCategory(catTx.size())
                    .percentageOfCategory(calculatePercentage(currentSpent, totalSpentGlobal))
                    .averageTransactionAmount(calculateAvgTx(currentSpent, catTx.size()))

                    // Budget Metrics
                    .budgetLimit(limit)
                    .budgetConsumed(currentSpent)
                    .budgetRemaining(hasBudget ? limit.subtract(currentSpent) : BigDecimal.ZERO)

                    // Health Flags
                    .overspent(hasBudget && currentSpent.compareTo(limit) > 0 ? currentSpent.subtract(limit) : BigDecimal.ZERO)
                    .isOverspent(hasBudget && currentSpent.compareTo(limit) > 0)
                    .isHalfReached(hasBudget && currentSpent.compareTo(limit.multiply(new BigDecimal("0.5"))) >= 0)
                    .isEightyReached(hasBudget && currentSpent.compareTo(limit.multiply(new BigDecimal("0.8"))) >= 0)
                    .isFullReached(hasBudget && currentSpent.compareTo(limit) >= 0)

                    // Trend Analysis
                    .trendPercentage(calculateTrend(currentSpent, prevSpent))

                    // Context
                    .periodStart(currentDates[0])
                    .periodEnd(currentDates[1])
                    .periodType(finance_service.revakh.models.Period.MONTHLY)
                    .build());
        }

        return dtos;
    }

    // =================================================================================
    //                                  HELPER METHODS
    // =================================================================================

    /**
     * Calculates what percentage 'part' is of 'total'.
     * Handles division by zero safely.
     */
    private BigDecimal calculatePercentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return part.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    /**
     * Calculates percentage change: ((Current - Previous) / Previous) * 100
     * Returns 100% if previous was 0 and current is positive.
     */
    private BigDecimal calculateTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            // If we spent 0 last month and 100 this month, that's technically a 100% (or infinite) increase.
            return current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100.00") : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * Calculates average transaction size.
     */
    private BigDecimal calculateAvgTx(BigDecimal total, int count) {
        if (count == 0) return BigDecimal.ZERO;
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    /**
     * Converts the user's PeriodPreset (e.g. LAST_30_DAYS) into actual LocalDate range.
     */
    private LocalDate[] resolveDates(PeriodPreset preset) {
        LocalDate now = LocalDate.now();
        switch (preset) {
            case LAST_7_DAYS: return new LocalDate[]{now.minusDays(6), now};
            case LAST_30_DAYS: return new LocalDate[]{now.minusDays(29), now};
            case THIS_MONTH: return new LocalDate[]{now.withDayOfMonth(1), now};
            case LAST_MONTH:
                LocalDate startLast = now.minusMonths(1).withDayOfMonth(1);
                return new LocalDate[]{startLast, startLast.plusMonths(1).minusDays(1)};
            default: return new LocalDate[]{now.minusDays(30), now};
        }
    }

    /**
     * Calculates the "Previous" period based on the current range.
     * If current is 10 days, previous is the 10 days before that.
     */
    private LocalDate[] getPreviousPeriodDates(LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        // We subtract 'days + 1' to ensure no overlap and exactly same duration
        return new LocalDate[]{start.minusDays(days + 1), start.minusDays(1)};
    }
}