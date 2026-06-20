package SmartExaminationSystem.SES.controller;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import SmartExaminationSystem.SES.model.Exam;
import SmartExaminationSystem.SES.model.Result;
import SmartExaminationSystem.SES.repository.ExamRepository;
import SmartExaminationSystem.SES.repository.*;

import SmartExaminationSystem.SES.model.Student;
import SmartExaminationSystem.SES.model.User;
import SmartExaminationSystem.SES.repository.StudentRepository;
import SmartExaminationSystem.SES.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Base64;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
 
@Controller
@RequestMapping("/admin")
public class AdminController {

    // ✅ THIS IS WHERE @Autowired GOES
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private QuestionRepository questionRepository; 
    
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ResultRepository resultRepository;
    // ================= ADMIN DASHBOARD =================
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        // Logged in admin
        User admin = (User) session.getAttribute("loggedInUser");

        if (admin == null) {
            return "redirect:/login";
        }

        // Fetch data
        List<Result> results = resultRepository.findAll();
        List<Student> students = studentRepository.findAll();
        List<Exam> exams = examRepository.findAll();

        // Current time
        LocalDateTime now = LocalDateTime.now();

        // Live Exams
        long liveExams = exams.stream()
                .filter(exam ->
                        exam.getStartTime() != null &&
                        exam.getEndTime() != null &&
                        now.isAfter(exam.getStartTime()) &&
                        now.isBefore(exam.getEndTime()))
                .count();

        // Upcoming Exams
        long upcomingExams = exams.stream()
                .filter(exam ->
                        exam.getStartTime() != null &&
                        exam.getStartTime().isAfter(now))
                .count();

        // Upcoming Exam List
        List<Exam> upcomingExamList = exams.stream()
                .filter(exam ->
                        exam.getStartTime() != null &&
                        exam.getStartTime().isAfter(now))
                .toList();

        // Model
        model.addAttribute("admin", admin);
        model.addAttribute("totalStudents", students.size());
        model.addAttribute("totalExams", exams.size());
        model.addAttribute("liveExams", liveExams);
        model.addAttribute("upcomingExams", upcomingExams);
        model.addAttribute("recentStudents", students);
        model.addAttribute("upcomingExamList", upcomingExamList);
        
        List<String> notifications = new ArrayList<>();

		     // 1. New Student Registered
		     if (!students.isEmpty()) {
		         Student latestStudent = students.get(students.size() - 1);
		
		         if (latestStudent.getName() != null &&
		             !latestStudent.getName().trim().isEmpty()) {
		
		             notifications.add(
		                 "New student registered: " +
		                 latestStudent.getName()
		             );
		         }
		     }
		
		     // 2. Face Registration Pending
		     long pendingFace = students.stream()
		             .filter(s ->
		                 s.getFaceImage() == null ||
		                 s.getFaceImage().trim().isEmpty()
		             )
		             .count();
		
		     if (pendingFace > 0) {
		         notifications.add(
		             pendingFace +
		             " student(s) pending face registration"
		         );
		     } else {
		         notifications.add(
		             "All students face registration completed"
		         );
		     }
		
		     // 3. Live Exams Running
		     if (liveExams > 0) {
		         notifications.add(
		             liveExams +
		             " live exam(s) running now"
		         );
		     }
		
		     // 4. Upcoming Exams
		     if (upcomingExams > 0) {
		         notifications.add(
		             upcomingExams +
		             " upcoming exam(s) scheduled"
		         );
		     } else {
		         notifications.add(
		             "No upcoming exams currently"
		         );
		     }
		
		     // 5. Results Published
		     if (results != null && !results.isEmpty()) {
		         notifications.add(
		             results.size() +
		             " result(s) available for review"
		         );
		     }
		
		     // 6. Exams Without Questions
		     long noQuestionExams = exams.stream()
		    	        .filter(e ->
		    	            questionRepository.countByExamId(e.getId()) == 0
		    	        )
		    	        .count();	
		
		     if (noQuestionExams > 0) {
		         notifications.add(
		             noQuestionExams +
		             " exam(s) have no questions assigned"
		         );
		     }
		
		     // 7. Students Not Attempted Any Exam
		     long inactiveStudents = students.stream()
		             .filter(student ->
		                 results.stream()
		                     .noneMatch(r ->
		                         r.getStudent() != null &&
		                         r.getStudent().getId()
		                          .equals(student.getId())
		                     )
		             )
		             .count();
		
		     if (inactiveStudents > 0) {
		         notifications.add(
		             inactiveStudents +
		             " student(s) have not attempted any exam"
		         );
		     }
		
		     // send to HTML
		     model.addAttribute(
		         "notifications",
		         notifications
		     );
	        return "admin-dashboard";
    }

    // ================= MANAGE EXAMS =================
    @GetMapping("/manage-exams")
    public String manageExams(Model model) {
        List<Exam> exams = examRepository.findAll();
        model.addAttribute("exams", exams);
        return "manage-exams";
    }

    // ================= CREATE EXAM =================
    @GetMapping("/exams/create")
    public String createExamForm(Model model) {
        model.addAttribute("exam", new Exam());
        return "create-exam";
    }

    @PostMapping("/exams/create")
    public String saveExam(@ModelAttribute Exam exam, Model model) {

        if (exam.getStartTime() != null && exam.getEndTime() != null &&
            exam.getEndTime().isBefore(exam.getStartTime())) {

            model.addAttribute("error", "End time must be after start time");
            return "create-exam";
        }

        examRepository.save(exam);
        return "redirect:/admin/exams";
    }
    
    //view exam page
    @GetMapping("/exams")
    public String viewExams(Model model) {

        List<Exam> exams = examRepository.findAll();

        Map<Long, Integer> questionCountMap = new HashMap<>();

        for (Exam exam : exams) {
            int count = questionRepository.countByExamId(exam.getId());
            questionCountMap.put(exam.getId(), count);
        }

        model.addAttribute("exams", exams);
        model.addAttribute("questionCountMap", questionCountMap);

        return "exams";
    }
    
    //Edit Exam 
    @GetMapping("/exams/edit/{id}")
    public String editExamForm(@PathVariable Long id, Model model) {

        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        model.addAttribute("exam", exam);

        return "edit-exam";
    }
    
    @PostMapping("/exams/update/{id}")
    public String updateExam(@PathVariable Long id,
                             @ModelAttribute Exam exam) {

        Exam existingExam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        existingExam.setExamName(exam.getExamName());
        existingExam.setSubject(exam.getSubject());
        existingExam.setDuration(exam.getDuration());
        existingExam.setTotalMarks(exam.getTotalMarks());

        existingExam.setStartTime(exam.getStartTime());
        existingExam.setEndTime(exam.getEndTime());
        existingExam.setCourse(exam.getCourse());	
        existingExam.setSemester(exam.getSemester());

        examRepository.save(existingExam);

        return "redirect:/admin/exams";
    }

    // ================= DELETE EXAM =================
    @Transactional
    @GetMapping("/exams/delete/{id}")
    public String deleteExam(@PathVariable Long id, Model model) {

        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        LocalDateTime now = LocalDateTime.now();

        // 🔥 CHECK IF LIVE
        if (exam.getStartTime() != null && exam.getEndTime() != null &&
            now.isAfter(exam.getStartTime()) && now.isBefore(exam.getEndTime())) {

        	return "redirect:/admin/exams?liveError";
        }

        questionRepository.deleteByExamId(id);
        examRepository.deleteById(id);

        return "redirect:/admin/exams";
    }

    // Student Register form //
    
    @GetMapping("/students/add")
    public String addStudentPage() {
        return "add-student";
    }
    @Transactional
    @PostMapping("/students/add")
    public String addStudent(
            @RequestParam String name,
            @RequestParam String fatherName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String mobile,
            @RequestParam String address,
            @RequestParam String rollNumber,
            @RequestParam String enrollmentNumber,
            @RequestParam String course,
            @RequestParam String branch,
            @RequestParam String semester,
            @RequestParam String section,
            @RequestParam String session,
            @RequestParam String gender,
            @RequestParam String dob,
            @RequestParam("capturedImage") String capturedImage,
            Model model) {

        try {

            // ================= USER =================
            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setRole("STUDENT");
            user.setUsername(email);
            user.setEmailVerified(true);
            user.setAdminApproved(true);

            userRepository.save(user);

            // ================= STUDENT =================
            Student s = new Student();
            s.setName(name);
            s.setFatherName(fatherName);
            s.setMobile(mobile);
            s.setAddress(address);
            s.setRollNumber(rollNumber);
            s.setEnrollmentNumber(enrollmentNumber);
            s.setCourse(course);
            s.setBranch(branch);
            s.setSemester(semester);
            s.setSection(section);
            s.setSession(session);
            s.setGender(gender);
            s.setDob(java.time.LocalDate.parse(dob));
            s.setUser(user);

            // default = pending face registration
            s.setFaceImage(null);

            // ================= IF FACE EXISTS =================
            if (capturedImage != null &&
                !capturedImage.trim().isEmpty() &&
                capturedImage.contains(",")) {

                try {
                    String base64Image = capturedImage.split(",")[1];
                    byte[] imageBytes =
                            Base64.getDecoder().decode(base64Image);

                    Path path = Paths.get(
                        "C:/Users/NIKHIL AGARWAL/OneDrive/Desktop/SES/face-recognition/raw_faces/"
                        + email.replace("@","_at_").replace(".","_")
                        + ".jpg"
                    );

                    Files.write(path, imageBytes);

                    RestTemplate restTemplate =
                            new RestTemplate();

                    MultiValueMap<String, Object> body =
                            new LinkedMultiValueMap<>();

                    body.add("email", email);
                    body.add("image",
                            new FileSystemResource(path.toFile()));

                    HttpHeaders headers =
                            new HttpHeaders();
                    headers.setContentType(
                            MediaType.MULTIPART_FORM_DATA
                    );

                    HttpEntity<MultiValueMap<String, Object>>
                            request =
                            new HttpEntity<>(body, headers);

                    restTemplate.postForObject(
                            "http://127.0.0.1:5000/register-face",
                            request,
                            String.class
                    );

                    // save only if face uploaded
                    s.setFaceImage(email + ".jpg");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    s.setFaceImage(null);
                }
            }
            
            // SAVE STUDENT LAST
            studentRepository.save(s);

        }catch (DataIntegrityViolationException e) {
            model.addAttribute(
                    "errorMessage",
                    "Duplicate Entry! Email / Roll Number / Enrollment Number already exists."
                );
                return "add-student";
            }
            catch (Exception e) {
                model.addAttribute(
                    "errorMessage",
                    "Something went wrong while saving student."
                );
                return "add-student";
            }

        return "redirect:/admin/students/list";
    }
    
    @GetMapping("/students/list")
    public String viewStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String course,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String updated,
            @RequestParam(required = false) String deleted,
            Model model,
            HttpSession session) {

        User admin = (User) session.getAttribute("loggedInUser");

        if (admin == null) {
            return "redirect:/login";
        }

        List<Student> students = studentRepository.searchStudents(
                (keyword == null || keyword.isBlank()) ? null : keyword,
                (course == null || course.isBlank()) ? null : course,
                (semester == null || semester.isBlank()) ? null : semester
        );

        List<Result> results = resultRepository.findAll();
        List<Exam> exams = examRepository.findAll();

        Map<Long, Long> attemptedMap = new HashMap<>();
        Map<Long, Long> notAttemptedMap = new HashMap<>();

        for (Student s : students) {

            long attempted = results.stream()
                    .filter(r -> r.getStudent() != null &&
                            r.getStudent().getId().equals(s.getId()))
                    .count();

            long totalAvailable = exams.stream()
                    .filter(e ->
                            e.getCourse() != null &&
                            e.getSemester() != null &&
                            s.getCourse() != null &&
                            s.getSemester() != null &&
                            e.getCourse().trim()
                                    .equalsIgnoreCase(
                                            s.getCourse().trim()) &&
                            e.getSemester().trim()
                                    .equalsIgnoreCase(
                                            s.getSemester().trim()))
                    .count();

            attemptedMap.put(s.getId(), attempted);
            notAttemptedMap.put(
                    s.getId(),
                    totalAvailable - attempted
            );
        }

        model.addAttribute("admin", admin);
        model.addAttribute("students", students);
        model.addAttribute("courses",
                studentRepository.findDistinctCourses());
        model.addAttribute("semesters",
                studentRepository.findDistinctSemesters());

        model.addAttribute("keyword", keyword);
        model.addAttribute("course", course);
        model.addAttribute("semester", semester);

        model.addAttribute("attemptedMap", attemptedMap);
        model.addAttribute("notAttemptedMap", notAttemptedMap);
        
        if (updated != null) {
            model.addAttribute(
                    "successMessage",
                    "Student updated successfully"
            );
        }

        if (deleted != null) {
            model.addAttribute(
                    "successMessage",
                    "Student deleted successfully"
            );
        }

        return "view-students";
    }
    
    @GetMapping("/students")
    public String manageStudents(Model model) {

        List<Student> students = studentRepository.findAll();

        // Pending face registration
        long pendingFace = students.stream()
                .filter(s ->
                        s.getFaceImage() == null ||
                        s.getFaceImage().trim().isEmpty())
                .count();

        // Active students
        long activeStudents = students.size() - pendingFace;

        // Send data to HTML
        model.addAttribute("totalStudents", students.size());
        model.addAttribute("pendingFaceCount", pendingFace);
        model.addAttribute("activeStudents", activeStudents);

        return "manage-students";
    }
    
    @GetMapping("/students/view/{id}")
    public String viewStudentDetail(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        User admin = (User) session.getAttribute("loggedInUser");

        if (admin == null) {
            return "redirect:/login";
        }

        Student student = studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        List<Result> results =
                resultRepository.findByStudent(student);

        List<Exam> exams =
                examRepository.findByCourseAndSemester(
                        student.getCourse(),
                        student.getSemester()
                );

        int totalExams = exams.size();
        int attempted = results.size();
        int notAttempted = totalExams - attempted;

        double avgScore = results.stream()
                .filter(r -> r.getTotal() > 0)
                .mapToDouble(r ->
                        ((double) r.getScore() / r.getTotal()) * 100)
                .average()
                .orElse(0);

        int highestScore = results.stream()
                .mapToInt(Result::getScore)
                .max()
                .orElse(0);

        LocalDateTime lastAttempt = results.stream()
                .map(Result::getSubmittedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        model.addAttribute("admin", admin);
        model.addAttribute("student", student);

        model.addAttribute("totalExams", totalExams);
        model.addAttribute("attempted", attempted);
        model.addAttribute("notAttempted", notAttempted);
        model.addAttribute("avgScore", Math.round(avgScore));
        model.addAttribute("highestScore", highestScore);
        model.addAttribute("lastAttempt", lastAttempt);
        
        

        return "student-detail";
    }
    
    @GetMapping("/students/edit/{id}")
    public String editStudentPage(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        User admin = (User) session.getAttribute("loggedInUser");

        if (admin == null) {
            return "redirect:/login";
        }

        Student student = studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        model.addAttribute("student", student);

        return "edit-student";
    }
    
    @PostMapping("/students/update/{id}")
    public String updateStudent(
            @PathVariable Long id,
            @ModelAttribute Student updatedStudent,
            @RequestParam(required = false) String capturedImage)
            throws IOException {

        Student student = studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        // normal fields
        student.setName(updatedStudent.getName());
        student.setFatherName(updatedStudent.getFatherName());
        student.setMobile(updatedStudent.getMobile());
        student.setAddress(updatedStudent.getAddress());
        student.setCourse(updatedStudent.getCourse());
        student.setBranch(updatedStudent.getBranch());
        student.setSemester(updatedStudent.getSemester());
        student.setSection(updatedStudent.getSection());

        // face upload
        if (capturedImage != null &&
        	    !capturedImage.trim().isEmpty() &&
        	    capturedImage.contains(",")) {

        	    try {
        	        String base64 =
        	                capturedImage.split(",")[1];

        	        byte[] imageBytes =
        	                Base64.getDecoder().decode(base64);

        	        String email =
        	                student.getUser().getEmail();

        	        String safeFile =
        	                email.replace("@","_at_")
        	                     .replace(".","_");

        	        Path path = Paths.get(
        	            "C:/Users/NIKHIL AGARWAL/OneDrive/Desktop/SES/face-recognition/raw_faces/"
        	            + safeFile + ".jpg"
        	        );

        	        Files.write(path, imageBytes);

        	        // FLASK REGISTER
        	        RestTemplate restTemplate =
        	                new RestTemplate();

        	        MultiValueMap<String,Object> body =
        	                new LinkedMultiValueMap<>();

        	        body.add("email", email);
        	        body.add("image",
        	                new FileSystemResource(path.toFile()));

        	        HttpHeaders headers =
        	                new HttpHeaders();

        	        headers.setContentType(
        	                MediaType.MULTIPART_FORM_DATA
        	        );

        	        HttpEntity<MultiValueMap<String,Object>> request =
        	                new HttpEntity<>(body, headers);

        	        restTemplate.postForObject(
        	                "http://127.0.0.1:5000/register-face",
        	                request,
        	                String.class
        	        );

        	        student.setFaceImage(email + ".jpg");

        	    } catch (Exception ex) {
        	        ex.printStackTrace();
        	    }
        	}
        studentRepository.save(student);

        return "redirect:/admin/students/list?updated";
    }
        
    
    @GetMapping("/students/delete/{id}")
    public String deleteStudent(
            @PathVariable Long id) {

        Student student =
                studentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found"));

        User user = student.getUser();

        studentRepository.delete(student);

        if (user != null) {
            userRepository.delete(user);
        }

        return "redirect:/admin/students/list?deleted";
    }
    
    @GetMapping("/students/face-status")
    public String faceRegistrationPage(Model model) {

        List<Student> pendingStudents =
                studentRepository.findByFaceImageIsNull();

        model.addAttribute("students", pendingStudents);

        return "face-status";
    }
    
    @GetMapping("/results")
    public String resultsDashboard(Model model){

        model.addAttribute(
            "totalResults",
            resultRepository.count()
        );

        model.addAttribute(
            "totalExams",
            examRepository.count()
        );

        model.addAttribute(
            "highestMarks",
            resultRepository.findHighestMarks()
        );

        return "admin-results";
    }
    
    @GetMapping("/results/exams")
    public String examResults(Model model) {

        List<Exam> exams = examRepository.findAll();

        // RESULT COUNT MAP
        Map<Long, Long> resultCountMap = new HashMap<>();

        for (Exam exam : exams) {

            long count =
                resultRepository.countByExam_Id(exam.getId());

            resultCountMap.put(exam.getId(), count);
        }

        model.addAttribute("exams", exams);

        // SEND TO HTML
        model.addAttribute("resultCountMap", resultCountMap);

        return "exam-wise-results";
    }
    
    @GetMapping("/results/exam/{id}")
    public String singleExamResults(@PathVariable Long id,
                                    Model model) {

        Exam exam =
            examRepository.findById(id).orElse(null);

        List<Result> results =
            resultRepository.findByExamIdOrderByScoreDesc(id);

        model.addAttribute("exam", exam);
        model.addAttribute("results", results);

        return "single-exam-results";
    }
    
    @GetMapping("/results/toppers")
    public String topPerformers(Model model) {

        List<Result> toppers =
                resultRepository
                .findTop10ByOrderByScoreDesc();

        model.addAttribute("toppers", toppers);

        return "top-performers";
    }
    
    @GetMapping("/results/students")
    public String studentWiseResults(Model model) {

        List<Result> results =
                resultRepository.findAll();

        model.addAttribute("results", results);

        return "student-wise-results";
    }
}
