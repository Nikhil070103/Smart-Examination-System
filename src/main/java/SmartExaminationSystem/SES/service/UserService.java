package SmartExaminationSystem.SES.service;

import java.util.Random;
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

        userRepository.findByEmail(user.getEmail()).ifPresent(existing -> {
            throw new RuntimeException("Email already registered");
        });

        String otp = String.format("%06d",
                new Random().nextInt(1000000));

        user.setRole("ADMIN");
        user.setEmailVerified(true);
        user.setAdminApproved(false);

        user.setOtp(otp);

        User savedUser = userRepository.save(user);

        emailService.sendAdminApprovalOtp(
                savedUser.getEmail(),
                savedUser.getUsername(),
                otp
        );
    }

    // ================= VERIFY EMAIL =================
    public boolean verifyOtp(String email, String otp) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return false;
        }

        if (!otp.equals(user.getOtp())) {
            return false;
        }

        user.setEmailVerified(true);
        user.setOtp(null);

        userRepository.save(user);

        return true;
    }

    // ================= LOGIN =================
    public User loginUser(String email, String password) {

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;

//        if (!user.getEmailVerified()) {
//            throw new RuntimeException("Please verify your email first");
//        }

        if ("ADMIN".equalsIgnoreCase(user.getRole())
                && !user.getAdminApproved()) {

            throw new RuntimeException(
                    "Waiting for Super Admin approval");
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

    // ================= FACE LOGIN SUPPORT =================
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    public boolean approveAdminByOtp(
            String email,
            String otp) {

        User user =
                userRepository.findByEmail(email)
                        .orElse(null);

        if (user == null) {
            return false;
        }

        if (!otp.equals(user.getOtp())) {
            return false;
        }

        user.setAdminApproved(true);
        user.setOtp(null);

        userRepository.save(user);

        return true;
    }
}
