package SmartExaminationSystem.SES.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= RESULT DATA =================
    private int score;

    private int total;

    private LocalDateTime submittedAt;

    // ================= EXAM RELATION =================
    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    // ================= STUDENT RELATION =================
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    // OPTIONAL (for quick display/history)
    private String studentName;

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    // SCORE
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // TOTAL
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    // SUBMITTED TIME
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    // EXAM
    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    // STUDENT
    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    // STUDENT NAME
    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
}