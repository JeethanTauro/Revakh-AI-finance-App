package finance_service.revakh.metrics.overview;


import finance_service.revakh.metrics.PeriodPreset;
import finance_service.revakh.metrics.overview.DTO.OverviewDTO;
import finance_service.revakh.models.TransactionLedger;
import finance_service.revakh.models.TransactionType;
import finance_service.revakh.repo.TransactionLedgerRepo;
import finance_service.revakh.repo.WalletRepo;
import finance_service.revakh.service.FinanceUserService;
import finance_service.revakh.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static finance_service.revakh.metrics.PeriodPreset.*;

@Service
@RequiredArgsConstructor
public class OverviewMetricsService {

    private final TransactionLedgerRepo transactionLedgerRepo;
    private final WalletService walletService;
    private final FinanceUserService financeUserService;
    public OverviewDTO getOverview(PeriodPreset periodPreset, Long userId){
        LocalDate[] dates = resolveDates(periodPreset);
        LocalDate start = dates[0];
        LocalDate end  = dates[1];

        List<TransactionLedger> transactionLedgerList = transactionLedgerRepo.findByUserIdAndDateBetween(userId, start, end);


        // 1. Calculate Totals
        BigDecimal totalIncome = calculateTotal(transactionLedgerList, TransactionType.CREDIT);
        BigDecimal totalExpense = calculateTotal(transactionLedgerList, TransactionType.DEBIT);
        BigDecimal totalSavings = totalIncome.subtract(totalExpense);

        // 2. Find Max Values
        BigDecimal largestIncome = findMax(transactionLedgerList, TransactionType.CREDIT);
        BigDecimal largestExpense = findMax(transactionLedgerList, TransactionType.DEBIT);

        BigDecimal currentBalance = walletService.getWallet(financeUserService.getUser(userId)).getBalance();


        long daysDiff = ChronoUnit.DAYS.between(start, end) + 1; // +1 to include end date

        BigDecimal avgExpense;
        BigDecimal avgIncome;
        String avgLabel; // To tell frontend what to display

        if (daysDiff <= 31) {
            // Short term -> Calculate DAILY Average
            avgExpense = totalExpense.divide(BigDecimal.valueOf(daysDiff), 2, RoundingMode.HALF_UP);
            avgIncome = totalIncome.divide(BigDecimal.valueOf(daysDiff), 2, RoundingMode.HALF_UP);
            avgLabel = "Daily Average";
        } else {
            // Long term -> Calculate MONTHLY Average
            long monthsDiff = ChronoUnit.MONTHS.between(start, end);
            if (monthsDiff < 1) monthsDiff = 1; // Safety check

            avgExpense = totalExpense.divide(BigDecimal.valueOf(monthsDiff), 2, RoundingMode.HALF_UP);
            avgIncome = totalIncome.divide(BigDecimal.valueOf(monthsDiff), 2, RoundingMode.HALF_UP);
            avgLabel = "Monthly Average";
        }

        // 5. Build DTO
        return OverviewDTO.builder()
                .periodStart(start)
                .periodEnd(end)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalSavings(totalSavings)
                .walletBalance(currentBalance)
                .totalTransactions(transactionLedgerList.size())
                .largestIncome(largestIncome)
                .largestExpense(largestExpense)
                .savingsPercentage(calculatePercentage(totalSavings, totalIncome))
                .averageExpensePerPeriod(avgExpense)
                .averageIncomePerPeriod(avgIncome)
                .averageType(avgLabel)
                .build();
    }
    private BigDecimal calculatePercentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return part.divide(total, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

       private LocalDate[] resolveDates(PeriodPreset periodPreset) {
        LocalDate now = LocalDate.now();
        switch (periodPreset) {
            case LAST_7_DAYS: return new LocalDate[]{now.minusDays(6), now};
            case LAST_30_DAYS: return new LocalDate[]{now.minusDays(29), now};
            case THIS_MONTH: return new LocalDate[]{now.withDayOfMonth(1), now};
            case LAST_MONTH:
                LocalDate startLast = now.minusMonths(1).withDayOfMonth(1);
                return new LocalDate[]{startLast, startLast.plusMonths(1).minusDays(1)};
            default: return new LocalDate[]{now.minusDays(30), now}; // Default fallback
        }
    }
    private BigDecimal calculateTotal(List<TransactionLedger> list, TransactionType type) {
        return list.stream()
                .filter(t -> t.getTransactionType() == type)
                .map(TransactionLedger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal findMax(List<TransactionLedger> list, TransactionType type) {
        return list.stream()
                .filter(t -> t.getTransactionType() == type)
                .map(TransactionLedger::getAmount)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

}
