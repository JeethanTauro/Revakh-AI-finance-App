package finance_service.revakh.repo;

import finance_service.revakh.models.FinanceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceUserRepo extends JpaRepository<FinanceUser, Long>{
}
