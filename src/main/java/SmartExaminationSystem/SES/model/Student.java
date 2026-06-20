package SmartExaminationSystem.SES.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👤 PERSONAL DETAILS
    private String name;
    private String fatherName;
    private LocalDate dob;
    private String gender;

    // 📞 CONTACT
    private String mobile;
    private String address;

    // 🎓 ACADEMIC
    @Column(unique = true)
    private String rollNumber;

    @Column(unique = true)
    private String enrollmentNumber;

    private String course;
    private String branch;
    private String semester;
    private String section;
    private String session;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String faceImage;
    
    // 🔗 LINK WITH USER (LOGIN)
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 🕒 SYSTEM
    private LocalDateTime createdAt = LocalDateTime.now();

    // ================= GETTERS & SETTERS =================

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getEnrollmentNumber() { return enrollmentNumber; }
    public void setEnrollmentNumber(String enrollmentNumber) { this.enrollmentNumber = enrollmentNumber; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getSession() { return session; }
    public void setSession(String session) { this.session = session; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
}