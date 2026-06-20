package SmartExaminationSystem.SES.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${ses.superadmin.email}")
    private String superAdminEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ================= USER EMAIL VERIFICATION =================
    public void sendVerificationEmail(String to, String token) {

        String link = baseUrl + "/verify?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Verify your Smart Examination account");
        msg.setText("Click the link below to verify your account:\n\n" + link);

        mailSender.send(msg);
    }

    // ================= SUPER ADMIN APPROVAL =================
    public void notifySuperAdmin(String email, String username, String token) {

        String link = baseUrl + "/superadmin/approve?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(superAdminEmail);
        msg.setSubject("Admin approval required");
        msg.setText(
                "A new admin registration requires approval\n\n" +
                "Username: " + username + "\n" +
                "Email: " + email + "\n\n" +
                "Approve here:\n" + link
        );

        mailSender.send(msg);
    }
    
    public void sendOtpEmail(String to, String otp) {

        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setTo(to);
        msg.setSubject("SES Verification OTP");

        msg.setText(
                "Your OTP is: " + otp +
                "\n\nOTP is valid for 10 minutes."
        );

        mailSender.send(msg);
    }
    
    public void sendAdminApprovalOtp(
            String adminEmail,
            String username,
            String otp) {

        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setTo(superAdminEmail);

        msg.setSubject("New Admin Approval Required");

        msg.setText(
                "New Admin Registration\n\n" +
                "Username: " + username + "\n" +
                "Email: " + adminEmail + "\n\n" +
                "Approval OTP: " + otp
        );

        mailSender.send(msg);
    }
}
