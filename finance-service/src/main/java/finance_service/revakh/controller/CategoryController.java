package finance_service.revakh.controller;

import finance_service.revakh.DTO.CategoryAddRequestDTO;
import finance_service.revakh.DTO.CategoryDTO;
import finance_service.revakh.exceptions.CategoryExceptions.CategoryExistsException;
import finance_service.revakh.exceptions.CategoryExceptions.CategoryNotFoundException;
import finance_service.revakh.exceptions.FinanceUserExceptions.UserNotFoundException;
import finance_service.revakh.models.Category;
import finance_service.revakh.models.CategoryType;
import finance_service.revakh.models.FinanceUser;
import finance_service.revakh.service.CategoryService;
import finance_service.revakh.service.FinanceUserService;
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
public class CategoryController {
    private final CategoryService categoryService;
    private final FinanceUserService financeUserService;


    //view all the categories
    @Operation(summary = "Get User Categories", description = "Retrieves all active categories (System + Custom) for a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of categories retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/categories")
    public ResponseEntity<?> getCategories(@PathVariable Long userId){
            List<Category> categories = categoryService.getAllCategory(userId);

            List<CategoryDTO> categoryDTOList = new ArrayList<>();
            for(Category category : categories){
                CategoryDTO categoryDTO = CategoryDTO.builder()
                        .categoryId(category.getCategoryId())
                        .name(category.getName())
                        .categoryType(category.getType())
                        .isActive(category.isActive())
                        .isSystem(category.isSystem())
                        .build();
                categoryDTOList.add(categoryDTO);
            }
            return ResponseEntity.ok(categoryDTOList);
    }


    //create new categories but only expense and income
    @Operation(summary = "Add Custom Category", description = "Creates a new custom category. Note: Income categories are restricted to SALARY and TOP_UP only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Category exists OR Invalid Income Type"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/{userId}/categories")
    public ResponseEntity<?> addCategory(@PathVariable Long userId, @Valid @RequestBody CategoryAddRequestDTO categoryAddRequestDTO){
            FinanceUser financeUser = financeUserService.getUser(userId);
            String categoryName = categoryAddRequestDTO.getCategoryName();
            CategoryType type = categoryAddRequestDTO.getType();
            categoryName = categoryName.trim();
            categoryName = categoryName.toUpperCase();
            Category category = categoryService.addCategory(financeUser,categoryName,type);
            CategoryDTO categoryDTO = CategoryDTO.builder()
                    .categoryId(category.getCategoryId())
                    .name(category.getName())
                    .categoryType(category.getType())
                    .isActive(category.isActive())
                    .isSystem(category.isSystem())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryDTO);
    }

    //delete/disable a category
    @Operation(summary = "Delete Category", description = "Soft deletes a custom category. Fails if the category has existing transactions or active budgets.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete: System category / Has Transactions / Has Budgets"),
            @ApiResponse(responseCode = "404", description = "Category or User not found")
    })
    @DeleteMapping("/{userId}/categories/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId, @PathVariable Long userId){
            FinanceUser financeUser = financeUserService.getUser(userId);
            categoryService.deleteOneCategory(financeUser,categoryId);
            return ResponseEntity.status(HttpStatus.OK).body("Deleted");
    }

}
