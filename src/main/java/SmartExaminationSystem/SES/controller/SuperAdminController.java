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
    public String approve(@RequestParam String token, Model model) {

        boolean approved = userService.approveAdmin(token);

        if (approved) {
            model.addAttribute("message", "Admin approved successfully");
        } else {
            model.addAttribute("error", "Invalid or expired approval link");
        }

        return "login";
    }
}
