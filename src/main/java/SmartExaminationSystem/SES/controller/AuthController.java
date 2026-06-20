package SmartExaminationSystem.SES.controller;
 
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import SmartExaminationSystem.SES.model.Exam;
import jakarta.servlet.http.HttpSession;
import SmartExaminationSystem.SES.model.User;
import SmartExaminationSystem.SES.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    /* ================= REGISTER ================= */

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {

            userService.registerUser(user);

            model.addAttribute(
                    "message",
                    "Registration submitted. Waiting for Super Admin approval."
            );

            return "redirect:/face-register?email=" + user.getEmail();

        } catch (Exception e) {

            model.addAttribute("error", e.getMessage());

            return "register";
        }
    }

    /* ================= FACE REGISTER PAGE ================= */

    @GetMapping("/face-register")
    public String showFaceRegister(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "face-register";
    }
    @GetMapping("/face-register-page")
    public String showFaceRegisterPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "face-register";
    }

    /* ================= EMAIL VERIFY ================= */

//    @GetMapping("/otp")
//    public String showOtpPage(
//            @RequestParam String email,
//            Model model) {
//
//        model.addAttribute("email", email);
//
//        return "otp";
//    }
//    
//    @PostMapping("/verify-otp")
//    public String verifyOtp(
//            @RequestParam String email,
//            @RequestParam String otp,
//            Model model) {
//
//        boolean verified =
//                userService.verifyOtp(email, otp);
//
//        if (verified) {
//
//            return "redirect:/face-register?email=" + email;
//        }
//
//        model.addAttribute("email", email);
//        model.addAttribute("error", "Invalid OTP");
//
//        return "otp";
//    }

    /* ================= LOGIN ================= */

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {

        User user = userService.loginUser(email, password);

        if (user == null) {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }

        session.setAttribute("loggedInUser", user);

        return "ADMIN".equalsIgnoreCase(user.getRole())
                ? "redirect:/admin/dashboard"
                : "redirect:/student/dashboard";
    }

    /* ================= FACE REGISTER API ================= */

    @PostMapping("/face-register")
    @ResponseBody
    public String faceRegister(
            @RequestParam("image") MultipartFile image,
            @RequestParam("email") String email
    ) {
        try {
            if (image.isEmpty() || email == null || email.isBlank()) {
                return "INVALID_REQUEST";
            }

            String flaskUrl = "http://localhost:5000/register-face";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // image
            ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return "face.jpg";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", imageResource);
            body.add("email", email); // ✅ MUST

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(flaskUrl, request, Map.class);

            Map<String, Object> responseBody = response.getBody();
            return (String) responseBody.get("status");

        } catch (HttpClientErrorException e) {
            // Flask 400 errors
            try {
                String body = e.getResponseBodyAsString();
                if (body.contains("NO_FACE")) return "NO_FACE";
                if (body.contains("MULTIPLE_FACE")) return "MULTIPLE_FACE";
                if (body.contains("LOW_QUALITY")) return "LOW_QUALITY";
                if (body.contains("INVALID_FACE")) return "INVALID_FACE";
            } catch (Exception ignore) {}

            return "FACE_REGISTER_FAILED";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }


    /* ================= FACE LOGIN ================= */

    @PostMapping("/face-login")
    @ResponseBody
    public String faceLogin(
            @RequestParam("image") MultipartFile image,
            HttpSession session
    ) {
        try {
            if (image.isEmpty()) {
                return "NO_IMAGE";
            }

            String flaskUrl = "http://localhost:5000/face-login";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource imageResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return "face.jpg";
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", imageResource);

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                    flaskUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            String status = (String) responseBody.get("status");

            // 🔴 Handle all Flask cases
            if ("NO_FACE".equals(status)) return "NO_FACE";
            if ("MULTIPLE_FACE".equals(status)) return "MULTIPLE_FACE";
            if ("LOW_QUALITY".equals(status)) return "LOW_QUALITY";
            if (!"MATCH".equals(status)) return "FACE_NOT_MATCHED";

            // ✅ MATCH
            String email = (String) responseBody.get("email");
            System.out.println("FACE LOGIN EMAIL = " + email);

            User user = userService.findByEmail(email);
            if (user == null) return "USER_NOT_FOUND";

            session.setAttribute("loggedInUser", user);

            return "ADMIN".equalsIgnoreCase(user.getRole())
                    ? "ADMIN_SUCCESS"
                    : "STUDENT_SUCCESS";

        } catch (HttpClientErrorException e) {
            // Flask may return JSON with 400
            try {
                String body = e.getResponseBodyAsString();
                if (body.contains("NO_FACE")) return "NO_FACE";
                if (body.contains("MULTIPLE_FACE")) return "MULTIPLE_FACE";
            } catch (Exception ignore) {}

            return "FACE_NOT_MATCHED";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    @GetMapping("/approve-otp")
    public String showOtpPage(
            @RequestParam String email,
            Model model) {

        model.addAttribute("email", email);

        return "approve-otp";
    }
    @PostMapping("/approve-otp")
    public String verifyApprovalOtp(
            @RequestParam String email,
            @RequestParam String otp,
            Model model) {

        boolean approved =
                userService.approveAdminByOtp(email, otp);

        if (approved) {

            model.addAttribute(
                    "message",
                    "Account approved successfully. Please login."
            );

            return "login";
        }

        model.addAttribute("email", email);
        model.addAttribute("error", "Invalid OTP");

        return "approve-otp";
    }
    /* ================= LOGOUT ================= */

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
   
}
