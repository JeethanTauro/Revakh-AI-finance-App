package finance_service.revakh.repo;

import finance_service.revakh.models.FinanceUser;
import finance_service.revakh.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepo extends JpaRepository<Wallet,Long> {
    Optional<Wallet> findByFinanceUser(FinanceUser financeUser);

    Optional<Wallet> findByFinanceUserAndIsActiveTrue(FinanceUser user);
}
