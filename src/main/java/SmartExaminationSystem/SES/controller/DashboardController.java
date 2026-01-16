package SmartExaminationSystem.SES.controller;
import org.springframework.ui.Model; 
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import SmartExaminationSystem.SES.model.User;
import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";   // admin-dashboard.html
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard() {
        return "student-dashboard"; // student-dashboard.html
    }
    
    @GetMapping("/student/profile")
    public String studentProfile(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    
    @GetMapping("/student/exams")
    public String viewExams() {
        return "view-exams";
    }
    
    @GetMapping("/student/results")
    public String viewResults() {
        return "my-result";
    }
    
     
    @GetMapping("/admin/students")
    public String manageStudents(HttpSession session) {
        return "manage-students";
    }

    @GetMapping("/admin/exams")
    public String manageExams(HttpSession session) {
        return "manage-exams";
    }

    @GetMapping("/admin/results")
    public String viewResults(HttpSession session) {
        return "admin-results";
    }


}
