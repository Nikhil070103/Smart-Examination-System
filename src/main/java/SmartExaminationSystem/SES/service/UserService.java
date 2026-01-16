package SmartExaminationSystem.SES.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import SmartExaminationSystem.SES.model.User;
import SmartExaminationSystem.SES.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // ================= REGISTER =================
    public void registerUser(User user) {

        // If email exists but not verified → allow re-register
        userRepository.findByEmail(user.getEmail()).ifPresent(existing -> {
            if (Boolean.TRUE.equals(existing.getEmailVerified())) {
                throw new RuntimeException("Email already registered");
            } else {
                userRepository.delete(existing); // cleanup old unverified entry
            }
        });

        user.setEmailVerified(false);
        user.setAdminApproved(false);
        user.setVerificationToken(UUID.randomUUID().toString());

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            user.setAdminApprovalToken(UUID.randomUUID().toString());
        }

        User savedUser = userRepository.save(user);

        // Send email verification
        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getVerificationToken()
        );

        // Notify super admin if ADMIN
        if ("ADMIN".equalsIgnoreCase(savedUser.getRole())) {
            emailService.notifySuperAdmin(
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getAdminApprovalToken()
            );
        }
    }

    // ================= VERIFY EMAIL =================
    public boolean verifyEmail(String token) {

        User user = userRepository.findByVerificationToken(token).orElse(null);
        if (user == null) return false;

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return true;
    }

    // ================= LOGIN =================
    public User loginUser(String email, String password) {

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;

        if (!user.getEmailVerified()) {
            throw new RuntimeException("Please verify your email");
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole()) && !user.getAdminApproved()) {
            throw new RuntimeException("Admin approval pending");
        }

        if (user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }

    // ================= SUPER ADMIN APPROVE =================
    public boolean approveAdmin(String token) {

        User admin = userRepository.findByAdminApprovalToken(token).orElse(null);
        if (admin == null) return false;

        admin.setAdminApproved(true);
        admin.setAdminApprovalToken(null);
        userRepository.save(admin);

        return true;
    }
}
