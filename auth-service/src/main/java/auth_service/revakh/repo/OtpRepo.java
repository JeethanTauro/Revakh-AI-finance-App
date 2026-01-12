package auth_service.revakh.repo;

import auth_service.revakh.models.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepo extends JpaRepository<OTP, Long> {
    public void deleteByUserEmail(String userEmail);
    public OTP findByUserEmail(String userEmail);
}
