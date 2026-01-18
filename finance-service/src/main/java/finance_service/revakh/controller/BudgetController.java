package finance_service.revakh.controller;



import finance_service.revakh.DTO.*;
import finance_service.revakh.exceptions.*;
import finance_service.revakh.exceptions.BudgetExceptions.BudgetAlreadyExistsException;
import finance_service.revakh.exceptions.BudgetExceptions.BudgetNotFoundException;
import finance_service.revakh.exceptions.CategoryExceptions.CategoryNotFoundException;
import finance_service.revakh.exceptions.FinanceUserExceptions.UserNotFoundException;
import finance_service.revakh.models.Budget;
import finance_service.revakh.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/finance/users")
public class BudgetController {
    private final BudgetService budgetService;

    @Operation(summary = "Create a new Budget", description = "Creates a budget for a specific category. Validates that the limit does not exceed wallet balance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Budget created successfully"),
            @ApiResponse(responseCode = "400", description = "Limit exceeds wallet OR Category is Income type"),
            @ApiResponse(responseCode = "404", description = "User or Category not found")
    })
    @PostMapping("/budget")
    public ResponseEntity<?> addBudget(@RequestHeader("userId") Long userId,@Valid @RequestBody BudgetRequestDTO budgetRequestDTO){
            budgetRequestDTO.setFinanceUserId(userId);
            Budget budget = budgetService.createBudget(budgetRequestDTO);
            BudgetDTO budgetDTO = BudgetDTO.builder()
                    .budgetId(budget.getBudgetId())
                    .categoryId(budget.getCategory().getCategoryId())
                    .categoryName(budget.getCategory().getName())
                    .limitAmount(budget.getLimitAmount())
                    .period(budget.getPeriod())
                    .periodStart(budget.getPeriodStart())
                    .periodEnd(budget.getPeriodEnd())
                    .isActive(budget.isActive())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(budgetDTO);
    }

    @Operation(summary = "Update Budget", description = "Updates limit or period. If period changes, creates a new budget and archives the old one.")
    @PutMapping("/budget/{budgetId}")
    public ResponseEntity<?> updateBudget(@RequestHeader("userId") Long userId,@PathVariable Long budgetId,@Valid @RequestBody BudgetUpdateDTO budgetUpdateDTO){
            budgetUpdateDTO.setUserId(userId);
            Budget budget = budgetService.updateBudget(budgetId,budgetUpdateDTO);
            BudgetDTO budgetDTO = BudgetDTO.builder()
                    .budgetId(budget.getBudgetId())
                    .categoryId(budget.getCategory().getCategoryId())
                    .categoryName(budget.getCategory().getName())
                    .limitAmount(budget.getLimitAmount())
                    .period(budget.getPeriod())
                    .periodStart(budget.getPeriodStart())
                    .periodEnd(budget.getPeriodEnd())
                    .isActive(budget.isActive())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(budgetDTO);
    }


    @Operation(summary = "Delete Budget", description = "Soft deletes a budget (sets active=false).")
    @DeleteMapping("/budget/{budgetId}")
    public ResponseEntity<?> deleteBudget(@PathVariable Long budgetId){
            budgetService.deleteOneBudget(budgetId);
            return ResponseEntity.status(HttpStatus.OK).body("Deleted");

    }

    //gets all budgets but no consumption data
    @Operation(summary = "Get All Budgets (Simple)", description = "Lists active budgets without calculating consumption data (Faster).")
    @GetMapping("/budgets")
    public ResponseEntity<?> getBudget(@RequestHeader("userId") Long userId){
            List<Budget> budgets = budgetService.getUserBudgets(userId);
            List<BudgetDTO> budgetDTOList = new ArrayList<>();
            for (Budget budget : budgets) {
                BudgetDTO budgetDTO = BudgetDTO.builder()
                        .budgetId(budget.getBudgetId())
                        .categoryId(budget.getCategory().getCategoryId())
                        .categoryName(budget.getCategory().getName())
                        .limitAmount(budget.getLimitAmount())
                        .period(budget.getPeriod())
                        .periodStart(budget.getPeriodStart())
                        .periodEnd(budget.getPeriodEnd())
                        .isActive(budget.isActive())
                        .build();
                budgetDTOList.add(budgetDTO);
            }
            return ResponseEntity.status(HttpStatus.OK).body(budgetDTOList);
    }

    //gives only consumption of one budget
    @Operation(summary = "Get Consumption for One Budget", description = "Calculates spent, remaining, and percentage for a specific budget.")
    @GetMapping("/budget/consumption/{budgetId}")
    public ResponseEntity<?> getConsumption(@PathVariable Long budgetId){
            ConsumptionDTO consumptionDTO = budgetService.computeConsumption(budgetId);
            return  ResponseEntity.status(HttpStatus.OK).body(consumptionDTO);

    }

    //get all budgets with all consumption info
    @Operation(summary = "Get Full Dashboard Data", description = "Lists all budgets WITH full consumption calculations.")
    @GetMapping("/budgets/detailed")
    public ResponseEntity<?> getBudgetsWithConsumption(@RequestHeader("userId") Long userId) {
            List<Budget> budgets = budgetService.getUserBudgets(userId);
            List<BudgetDetailedDTO> response = new ArrayList<>();

            for (Budget budget : budgets) {
                ConsumptionDTO c = budgetService.computeConsumption(budget.getBudgetId());

                BudgetDetailedDTO dto = BudgetDetailedDTO.builder()
                        .budgetId(budget.getBudgetId())
                        .categoryId(budget.getCategory().getCategoryId())
                        .categoryName(budget.getCategory().getName())
                        .limitAmount(budget.getLimitAmount())
                        .period(budget.getPeriod())
                        .periodStart(budget.getPeriodStart())
                        .periodEnd(budget.getPeriodEnd())
                        .isActive(budget.isActive())

                        .spentAmount(c.getSpentAmount())
                        .remainingAmount(c.getRemainingAmount())
                        .percentageUsed(c.getPercentageUsed())
                        .overspent(c.getOverspent())
                        .isHalfReached(c.isHalfReached())
                        .isEightyReached(c.isEightyReached())
                        .isFullReached(c.isFullReached())
                        .budgetExceeded(c.isBudgetExceeded())
                        .build();

                response.add(dto);
            }
            return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //this endpoint is mainly for testing to see if the reset of periods in budgets work or not
    @Operation(summary = "resets expired budgets for all users")
    @GetMapping("/budgets/reset")
    public ResponseEntity<?> restBudget(){
        budgetService.resetExpiredBudgets();
        return ResponseEntity.ok("Rest all expired budgets");
    }

}
