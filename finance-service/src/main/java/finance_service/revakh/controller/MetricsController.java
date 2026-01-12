package finance_service.revakh.controller;

import finance_service.revakh.metrics.MetricName;
import finance_service.revakh.metrics.PeriodPreset;
import finance_service.revakh.metrics.category.CategoryMetricsService;
import finance_service.revakh.metrics.overview.OverviewMetricsService;
import finance_service.revakh.metrics.trends.TrendsMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // Base path matches your other controllers
@RequiredArgsConstructor
@Tag(name = "Financial Metrics", description = "Endpoints for Dashboard Analytics, Graphs, and Health Checks")
public class MetricsController {

    private final OverviewMetricsService overviewService;
    private final CategoryMetricsService categoryService;
    private final TrendsMetricsService trendsService;

    // --- 1. OVERVIEW DASHBOARD ---
    @Operation(summary = "Get Dashboard Overview", description = "Returns high-level totals (Income, Expense, Savings) and Net Cash Flow.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overview data retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/metrics/overview")
    public ResponseEntity<?> getOverview(
            @PathVariable Long userId,
            @Parameter(description = "Time range (default: LAST_30_DAYS)")
            @RequestParam(defaultValue = "LAST_30_DAYS") PeriodPreset preset) {

        return ResponseEntity.ok(overviewService.getOverview(preset, userId));
    }

    // --- 2. CATEGORY BREAKDOWN ---
    @Operation(summary = "Get Category Health", description = "Detailed breakdown per category (Spending vs Budget, Trends, Overspending flags).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category breakdown retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/metrics/categories")
    public ResponseEntity<?> getCategoryBreakdown(
            @PathVariable Long userId,
            @Parameter(description = "Time range (default: THIS_MONTH)")
            @RequestParam(defaultValue = "THIS_MONTH") PeriodPreset preset) {

        return ResponseEntity.ok(categoryService.getCategoryBreakdown(preset, userId));
    }

    // --- 3. TREND GRAPHS ---
    @Operation(summary = "Get Trend Graph Data", description = "Returns daily data points for line graphs (Income, Expense, or Savings). Guaranteed no gaps in dates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trend data retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/metrics/trends")
    public ResponseEntity<?> getTrends(
            @PathVariable Long userId,
            @Parameter(description = "Metric to graph (EXPENSE, INCOME, SAVINGS)")
            @RequestParam(defaultValue = "EXPENSE") MetricName metric,
            @Parameter(description = "Time range (default: LAST_30_DAYS)")
            @RequestParam(defaultValue = "LAST_30_DAYS") PeriodPreset preset) {

        return ResponseEntity.ok(trendsService.getTrends(metric, preset, userId));
    }
}