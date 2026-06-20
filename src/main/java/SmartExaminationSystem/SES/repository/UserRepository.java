package SmartExaminationSystem.SES.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import SmartExaminationSystem.SES.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByAdminApprovalToken(String token);

    boolean existsByEmail(String email);
}