package SmartExaminationSystem.SES.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String examName;
    private String subject;
    private int duration;
    private int totalMarks;
    private String course;
    private String semester;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;
 
    // CASCADE DELETE
    
    @OneToMany(mappedBy = "exam",
            cascade = CascadeType.ALL,
            orphanRemoval = true)

    private List<Result> results;

    @OneToMany(mappedBy = "exam",
            cascade = CascadeType.ALL,
            orphanRemoval = true)

    private List<Question> questions;

    // ===== getters & setters =====
    public Long getId() {
        return id;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getSemester() {
		return semester;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}
	
	public List<Result> getResults() {
	    return results;
	}

	public void setResults(List<Result> results) {
	    this.results = results;
	}

	public List<Question> getQuestions() {
	    return questions;
	}

	public void setQuestions(List<Question> questions) {
	    this.questions = questions;
	}
	
	public String toString() {
		return examName; 
	}
}
