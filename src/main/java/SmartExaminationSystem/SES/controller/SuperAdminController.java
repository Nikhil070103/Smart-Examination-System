package SmartExaminationSystem.SES.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import SmartExaminationSystem.SES.service.UserService;
@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/approve")
    public String showApprovalPage(
            @RequestParam String email,
            Model model) {

        model.addAttribute("email", email);

        return "superadmin-otp";
    }

    @PostMapping("/approve")
    public String approveAdmin(
            @RequestParam String email,
            @RequestParam String otp,
            Model model) {

        boolean approved =
                userService.approveAdminByOtp(email, otp);

        if (approved) {

            model.addAttribute(
                    "message",
                    "Admin approved successfully"
            );

            return "login";
        }

        model.addAttribute("email", email);
        model.addAttribute("error", "Invalid OTP");

        return "superadmin-otp";
    }
}