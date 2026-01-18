package finance_service.revakh.repo;

import finance_service.revakh.models.Budget;
import finance_service.revakh.models.Category;
import finance_service.revakh.models.FinanceUser;
import finance_service.revakh.models.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepo extends JpaRepository<Budget,Long>{
    @Query("""
    SELECT b FROM Budget b
    WHERE b.financeUser = :user AND b.isActive = true
    ORDER BY
        CASE WHEN b.category IS NULL THEN 0 ELSE 1 END,
        b.category.name ASC,
        b.periodStart ASC
""")
    List<Budget> findActiveBudgetsSorted(FinanceUser user);

    List<Budget> findAllByIsActiveTrue();

    // === FIX 1: Added explicit Query for this boolean check ===
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Budget b WHERE b.financeUser = :user AND b.category = :category AND b.period = :period AND b.isActive = true")
    boolean existsActiveBudget(@Param("user") FinanceUser user, @Param("category") Category category, @Param("period") Period period);

    // === FIX 2: Added explicit Query (Spring cannot parse 'existsActiveGlobalBudget' automatically) ===
    @Query("""
        SELECT COUNT(b) > 0 FROM Budget b
        WHERE b.financeUser = :financeUser
          AND b.category IS NULL
          AND b.period = :period
          AND b.isActive = true
    """)
    boolean existsActiveGlobalBudget(@Param("financeUser") FinanceUser financeUser, @Param("period") Period period);

    boolean existsByCategoryAndIsActiveTrue(Category category);

    Optional<List<Budget>> findAllByFinanceUserAndIsActiveTrue(FinanceUser financeUser);

    @Query("SELECT b FROM Budget b WHERE b.category.categoryId = :categoryId AND b.financeUser.userId = :userId")
    Optional<Budget> findByCategoryIdAndUserId(@Param("categoryId") Long categoryId, @Param("userId") Long userId);

    List<Budget> findAllByFinanceUser_UserId(Long userId);

    List<Budget> findAllByCategoryAndIsActiveTrue(Category category);
}
