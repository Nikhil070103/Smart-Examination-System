package SmartExaminationSystem.SES.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import SmartExaminationSystem.SES.model.User;
import SmartExaminationSystem.SES.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user);
            model.addAttribute("message", "Check your email to verify account");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, Model model) {
        boolean verified = userService.verifyEmail(token);
        if (verified) model.addAttribute("message", "Email verified successfully! Please login.");
        else model.addAttribute("error", "Invalid or expired verification link");
        return "login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
        try {
            User user = userService.loginUser(email, password);
            if (user == null) {
                model.addAttribute("error", "Invalid credentials");
                return "login";
            }
            session.setAttribute("loggedUser", user);
            if ("ADMIN".equalsIgnoreCase(user.getRole())) return "redirect:/admin/dashboard";
            else return "redirect:/student/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
