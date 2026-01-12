package finance_service.revakh.repo;

import finance_service.revakh.models.Category;
import finance_service.revakh.models.FinanceUser;
import finance_service.revakh.models.TransactionLedger;
import finance_service.revakh.models.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionLedgerRepo extends JpaRepository<TransactionLedger, Long> {

    // Used for soft delete checks later
    boolean existsByCategory(Category category);
    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM TransactionLedger t
    WHERE t.financeUser = :user
      AND t.transactionType = finance_service.revakh.models.TransactionType.DEBIT
      AND t.isDeleted = false
      AND t.date BETWEEN :start AND :end
      AND (:category IS NULL OR t.category = :category)
""")
    BigDecimal computeSpent(
            @Param("user") FinanceUser user,
            @Param("category") Category category,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    List<TransactionLedger> findAllByFinanceUserAndIsDeletedFalse(FinanceUser financeUser);


    Optional<TransactionLedger> findByFinanceUserAndTransactionIdAndIsDeletedFalse(FinanceUser financeUser, Long transactionId);

    // === FIX 1: Explicitly map userId to financeUser.userId ===
    @Query("SELECT t FROM TransactionLedger t WHERE t.financeUser.userId = :userId AND t.date BETWEEN :start AND :end")
    List<TransactionLedger> findByUserIdAndDateBetween(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // === FIX 2: Explicitly map userId here too ===
    @Query("SELECT t FROM TransactionLedger t WHERE t.financeUser.userId = :userId AND t.transactionType = :type AND t.date BETWEEN :start AND :end")
    List<TransactionLedger> findByUserIdAndTransactionTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
