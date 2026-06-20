package SmartExaminationSystem.SES.controller;

import org.springframework.ui.Model; 
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import SmartExaminationSystem.SES.model.Exam;
import SmartExaminationSystem.SES.model.Result;
import SmartExaminationSystem.SES.model.Student;
import SmartExaminationSystem.SES.model.User;
import SmartExaminationSystem.SES.repository.ExamRepository;
import SmartExaminationSystem.SES.repository.ResultRepository;
import SmartExaminationSystem.SES.repository.StudentRepository;
import SmartExaminationSystem.SES.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ResultRepository resultRepository;
    


//    @GetMapping("/student/dhboard")
//    public String studentDashboard() {
//        return "student-dashboard";
//    }
    
    @GetMapping("/student/profile")
    public String studentProfile(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Student student = studentRepository.findByUser(user);

        model.addAttribute("student", student);

        return "profile";
    }
    
    @GetMapping("/student/profile/edit")
    public String editProfile(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Student student = studentRepository.findByUser(user);

        model.addAttribute("student", student);

        return "update-profile";
    }


    @PostMapping("/student/profile/update")
    public String updateProfile(
            @ModelAttribute Student updatedStudent,
            HttpSession session) {

        User user =
            (User) session.getAttribute("loggedInUser");

        Student student =
            studentRepository.findByUser(user);

        // editable fields only
        student.setName(updatedStudent.getName());
        student.setMobile(updatedStudent.getMobile());
        if (!updatedStudent.getMobile().matches("\\d{10}")) {
            return "redirect:/student/profile?error=invalidMobile";
        }
        student.setAddress(updatedStudent.getAddress());

        studentRepository.save(student);

        return "redirect:/student/profile";
    }

    // ✅ FACE LOGIN SESSION FIX
    @GetMapping("/face-login-success")
    public String faceLoginSuccess(@RequestParam String email,
                                   HttpSession session) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        session.setAttribute("loggedInUser", user);

        // ROLE BASED REDIRECT
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        if ("TEACHER".equalsIgnoreCase(user.getRole())) {
            return "redirect:/teacher/dashboard";
        }

        if ("STUDENT".equalsIgnoreCase(user.getRole())) {
            return "redirect:/student/dashboard";
        }

        return "redirect:/login";
    }

    @GetMapping("/student")
    public String viewExams() {
        return "student-exams";
    }
    
    @GetMapping("/student/results")
    public String viewResults() {
        return "my-result";
    }
    
//    @GetMapping("/admin/students")
//    public String manageStudents() {
//        return "manage-students";
//    }
//
//    @GetMapping("/admin/results")
//    public String viewResultsAdmin() {
//        return "admin-results";
//    }
    
    @GetMapping("/student/dashboard")
    public String studentDashboard(Model model, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Student student = studentRepository.findByUser(user);
        if (student == null) return "redirect:/login";

        List<Exam> exams = examRepository.findByCourseAndSemester(
                student.getCourse().trim(),
                student.getSemester().trim()
        );

        List<Result> results = resultRepository.findByStudent(student);

        int totalExams = exams.size();
        int completedExams = results.size();

        List<Exam> upcomingOnly = exams.stream()
                .filter(e -> e.getStartTime() != null)
                .filter(e -> e.getStartTime().isAfter(LocalDateTime.now()))
                .toList();

        double avgScore = results.stream()
                .filter(r -> r.getTotal() > 0)
                .mapToDouble(r ->
                        ((double) r.getScore() / r.getTotal()) * 100)
                .average()
                .orElse(0);

        model.addAttribute("student", student);
        model.addAttribute("totalExams", totalExams);
        model.addAttribute("completedExams", completedExams);
        model.addAttribute("upcomingExams", upcomingOnly.size());
        model.addAttribute("averageScore", Math.round(avgScore));
        model.addAttribute("results", results);
        model.addAttribute("upcomingExamList", upcomingOnly);
        model.addAttribute("notifications", upcomingOnly);

        return "student-dashboard";
    }
    
    @GetMapping("/student/notifications")
    public String notifications(Model model,
                                HttpSession session) {

        User user =
            (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Student student =
            studentRepository.findByUser(user);

        List<Exam> exams =
            examRepository.findByCourseAndSemester(
                student.getCourse().trim(),
                student.getSemester().trim()
            );

        List<Exam> upcomingOnly = exams.stream()
            .filter(exam ->
                exam.getStartTime() != null &&
                exam.getStartTime().isAfter(LocalDateTime.now()))
            .toList();

        model.addAttribute("student", student);
        model.addAttribute("notifications", upcomingOnly);
        //model.addAttribute("notifications", upcomingOnly);
        return "notifications";
    }
    
   
}