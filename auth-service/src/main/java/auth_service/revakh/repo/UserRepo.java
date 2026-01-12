package auth_service.revakh.repo;

import auth_service.revakh.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findByUserName(String userName);
    Optional<User> findByUserEmail(String userEmail);

    boolean existsByUserEmail(String newEmail);
}
