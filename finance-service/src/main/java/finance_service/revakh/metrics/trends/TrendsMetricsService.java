package finance_service.revakh.metrics.trends;

import finance_service.revakh.metrics.MetricName;
import finance_service.revakh.metrics.PeriodPreset;
import finance_service.revakh.metrics.trends.DTO.TimeSeriesPointDTO;
import finance_service.revakh.metrics.trends.DTO.TrendsResponseDTO;
import finance_service.revakh.models.Period;
import finance_service.revakh.models.TransactionLedger;
import finance_service.revakh.models.TransactionType;
import finance_service.revakh.repo.TransactionLedgerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrendsMetricsService {

    private final TransactionLedgerRepo transactionLedgerRepo;

    /**
     * Generates a continuous timeline of financial data for graphs.
     * Guaranteed to return a point for EVERY day/unit in the range, even if value is 0.
     */
    public TrendsResponseDTO getTrends(MetricName metric, PeriodPreset preset, Long userId) {

        // --- STEP 1: RESOLVE DATES ---
        LocalDate[] dates = resolveDates(preset);
        LocalDate start = dates[0];
        LocalDate end = dates[1];

        // --- STEP 2: FETCH RAW DATA ---
        // Fetch all transactions in this range for this user.
        List<TransactionLedger> transactions = transactionLedgerRepo.findByUserIdAndDateBetween(userId, start, end);

        // --- STEP 3: AGGREGATE IN MEMORY (FIXED LOGIC) ---
        Map<LocalDate, BigDecimal> dailySums;

        if (metric == MetricName.SAVINGS) {
            // FIX: For Savings, we don't filter rows. We sum them all up.
            // Logic: Savings = Sum(CREDIT) - Sum(DEBIT)
            dailySums = transactions.stream()
                    .collect(Collectors.groupingBy(
                            TransactionLedger::getDate,
                            Collectors.reducing(
                                    BigDecimal.ZERO,
                                    // Mapper: Convert Transaction to signed amount (+ for Credit, - for Debit)
                                    t -> t.getTransactionType() == TransactionType.CREDIT
                                            ? t.getAmount()
                                            : t.getAmount().negate(),
                                    // Reducer: Add them up
                                    BigDecimal::add
                            )
                    ));
        } else {
            // Standard Logic (Income or Expense) - Filter first, then sum
            dailySums = transactions.stream()
                    .filter(t -> isMetricMatch(t, metric))
                    .collect(Collectors.groupingBy(
                            TransactionLedger::getDate,
                            Collectors.reducing(BigDecimal.ZERO, TransactionLedger::getAmount, BigDecimal::add)
                    ));
        }

        // --- STEP 4: GENERATE CONTINUOUS TIMELINE (THE GAP FILLER) ---
        List<TimeSeriesPointDTO> points = new ArrayList<>();
        LocalDate current = start;

        // Formatter for clean UI labels (e.g., "05 Jan")
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("dd MMM");

        while (!current.isAfter(end)) {
            // Check if we have data for this specific day. If not, default to ZERO.
            BigDecimal amount = dailySums.getOrDefault(current, BigDecimal.ZERO);

            points.add(TimeSeriesPointDTO.builder()
                    .date(current) // Backend logic uses this
                    .timeLabel(current.format(labelFormatter)) // Frontend graph uses this
                    .periodType(Period.DAILY) // Currently defaulting to Daily granularity
                    .amount(amount)
                    .currency("USD") // Can be dynamic based on User settings
                    .build());

            // Move to next day
            current = current.plusDays(1);
        }

        // --- STEP 5: BUILD RESPONSE ---
        return TrendsResponseDTO.builder()
                .userId(userId)
                .metricName(metric)
                .periodType(Period.DAILY)
                .preset(preset)
                .startDate(start)
                .endDate(end)
                .totalPoints(points.size()) // e.g., 30 points for LAST_30_DAYS
                .timeSeriesPointDTOList(points)
                .rangeDescription(formatRangeDescription(preset, start, end))
                .build();
    }

    // =================================================================================
    //                                  HELPER METHODS
    // =================================================================================

    /**
     * Filters transactions based on the requested MetricName.
     * Note: This is only used for INCOME and EXPENSE now.
     */
    private boolean isMetricMatch(TransactionLedger t, MetricName metric) {
        if (metric == MetricName.EXPENSE) {
            return t.getTransactionType() == TransactionType.DEBIT;
        }
        if (metric == MetricName.INCOME) {
            return t.getTransactionType() == TransactionType.CREDIT;
        }
        return false;
    }

    /**
     * Helper to create a human-readable description of the range.
     */
    private String formatRangeDescription(PeriodPreset preset, LocalDate start, LocalDate end) {
        if (preset == PeriodPreset.LAST_7_DAYS) return "Last 7 Days";
        if (preset == PeriodPreset.LAST_30_DAYS) return "Last 30 Days";
        if (preset == PeriodPreset.THIS_MONTH) return "This Month";
        // Fallback for custom ranges
        return start.format(DateTimeFormatter.ofPattern("dd MMM")) + " - " + end.format(DateTimeFormatter.ofPattern("dd MMM"));
    }

    private LocalDate[] resolveDates(PeriodPreset preset) {
        LocalDate now = LocalDate.now();
        return switch (preset) {
            case LAST_7_DAYS -> new LocalDate[]{now.minusDays(6), now};
            case LAST_30_DAYS -> new LocalDate[]{now.minusDays(29), now};
            case THIS_MONTH -> new LocalDate[]{now.withDayOfMonth(1), now};
            case LAST_MONTH -> {
                LocalDate startLast = now.minusMonths(1).withDayOfMonth(1);
                yield new LocalDate[]{startLast, startLast.plusMonths(1).minusDays(1)};
            }
            case THIS_YEAR -> new LocalDate[]{now.withDayOfYear(1), now};
            case LAST_YEAR -> {
                LocalDate startYear = now.minusYears(1).withDayOfYear(1);
                yield new LocalDate[]{startYear, startYear.plusYears(1).minusDays(1)};
            }
            default -> new LocalDate[]{now.minusDays(30), now};
        };
    }
}