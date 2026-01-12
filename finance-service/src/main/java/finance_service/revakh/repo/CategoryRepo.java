package finance_service.revakh.repo;

import finance_service.revakh.models.Category;
import finance_service.revakh.models.FinanceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {

    boolean existsByFinanceUser(FinanceUser financeUser);

    Optional<Category> findByFinanceUserAndNameAndIsActiveTrue(FinanceUser user, String name);

    List<Category> findAllByFinanceUserAndIsActiveTrue(FinanceUser user);

    boolean existsByFinanceUserAndNameIgnoreCaseAndIsActiveTrue(FinanceUser user, String normalizedName);

    Optional<Category> findByFinanceUserAndNameIgnoreCaseAndIsActiveTrue(FinanceUser user, String normalizedName);

    // FIX 1: Changed 'Id' to 'CategoryId' because your entity uses 'categoryId'
    // Alternatively, using @Query for safety:
    @Query("SELECT c FROM Category c WHERE c.financeUser = :financeUser AND c.categoryId = :categoryId")
    Optional<Category> findByFinanceUserAndCategoryId(@Param("categoryId") Long categoryId, @Param("financeUser") FinanceUser financeUser);

    // FIX 2: Explicitly map 'userId' to 'financeUser.userId'
    @Query("SELECT c FROM Category c WHERE c.financeUser.userId = :userId")
    List<Category> findAllByUserId(@Param("userId") Long userId);
}
