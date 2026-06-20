package SmartExaminationSystem.SES.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import SmartExaminationSystem.SES.model.Exam;
import SmartExaminationSystem.SES.model.Question;
import SmartExaminationSystem.SES.model.Result;
import SmartExaminationSystem.SES.model.Student;
import SmartExaminationSystem.SES.model.User;
import SmartExaminationSystem.SES.repository.ExamRepository;
import SmartExaminationSystem.SES.repository.QuestionRepository;
import SmartExaminationSystem.SES.repository.ResultRepository;
import jakarta.servlet.http.HttpSession;

import SmartExaminationSystem.SES.repository.StudentRepository;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private ResultRepository resultRepository;
    
    @Autowired
    private StudentRepository studentRepository;

    // ✅ SHOW EXAMS
    @GetMapping("/student-exams")
    public String viewAvailableExams(
            Model model,
            HttpSession session) {

        User user =
            (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // only student
        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            return "redirect:/login";
        }

        Student student =
            studentRepository.findByUser(user);

        if (student == null) {
            return "redirect:/login";
        }

        String course = student.getCourse();
        String semester = student.getSemester();

        List<Exam> exams = examRepository.findByCourseAndSemester(
                student.getCourse().trim(),
                student.getSemester().trim()
        );

        Map<Long, Boolean> submittedMap = new HashMap<>();

        for (Exam exam : exams) {
            boolean submitted =
                    resultRepository.existsByStudentAndExam(
                            student,
                            exam
                    );

            submittedMap.put(exam.getId(), submitted);
        }

        model.addAttribute("exams", exams);
        model.addAttribute("submittedMap", submittedMap);

        return "student-exams";
    }

    // ✅ START EXAM
   

    @GetMapping("/exams/{id}/start")
    public String startExam(@PathVariable Long id, Model model, HttpSession session) {

        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        List<Question> questions = questionRepository.findByExamId(id);

        LocalDateTime startTime;

        if (session.getAttribute("startTime") == null) {
            startTime = LocalDateTime.now();
            session.setAttribute("startTime", startTime);
        } else {
            startTime = (LocalDateTime) session.getAttribute("startTime");
        }

        long elapsed = Duration.between(startTime, LocalDateTime.now()).getSeconds();
        long totalTime = exam.getDuration() * 60;

        long remaining = totalTime - elapsed;

        if (remaining < 0) remaining = 0;

        model.addAttribute("questions", questions);
        model.addAttribute("examId", id);
        model.addAttribute("remainingTime", remaining); // 🔥 important

        return "start-exam";
    }
    // ✅ SUBMIT EXAM + CALCULATE RESULT
    @PostMapping("/exams/submit")
    public String submitExam(@RequestParam Long examId,
                             @RequestParam Map<String, String> answers,
                             Model model,
                             HttpSession session) {

        session.removeAttribute("startTime");

        User user = (User) session.getAttribute("loggedInUser");
        Student student = studentRepository.findByUser(user);

        if (student == null) {
            return "redirect:/login";
        }

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        // ================= BLOCK RE-ATTEMPT =================
        if (resultRepository.existsByStudentAndExam(student, exam)) {
            return "redirect:/student/student-exams";
        }

        // ================= CHECK ANSWERS =================
        List<Question> questions =
                questionRepository.findByExamId(examId);

        int score = 0;

        for (Question q : questions) {

            String selected =
                    answers.get(String.valueOf(q.getId()));

            if (selected != null &&
                selected.equalsIgnoreCase(
                        q.getCorrectAnswer())) {
                score++;
            }
        }
        int total = questions.size();
        if (questions.isEmpty()) {
            return "redirect:/student/student-exams";
        }

        // ================= SAVE RESULT =================
        Result result = new Result();

        result.setScore(score);
        result.setTotal(total);
        result.setExam(exam);
        result.setStudent(student);
        result.setStudentName(student.getName());
        result.setSubmittedAt(LocalDateTime.now());
        

        double percentage = ((double) score / total) * 100;
        resultRepository.save(result);

        // ================= SHOW RESULT =================
        model.addAttribute("score", score);
        model.addAttribute("total", total);
        model.addAttribute("percentage", percentage);
        
        System.out.println("Student ID = " + student.getId());
        System.out.println("Student Name = " + student.getName());

        result.setStudentName(student.getName());

        System.out.println("Result Name = " + result.getStudentName());

        return "result";
    }
    
	    @GetMapping("/my-results")
	    public String myResults(Model model, HttpSession session) {
	
	        User user = (User) session.getAttribute("loggedInUser");
	
	        Student student = studentRepository.findByUser(user);
	
	        List<Result> results = resultRepository.findByStudent(student);
	
	        model.addAttribute("results", results);
	
	        return "my-result";
	    }
	    
	   
	    
}