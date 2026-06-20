package SmartExaminationSystem.SES.model;

import jakarta.persistence.*;

@Entity  
public class Question {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String questionTitle;
	
	private String optionA;
	private String optionB;
	private String optionC;
	private String optionD;
	
	private String correctAnswer;
	
	@ManyToOne 
	@JoinColumn(name = "exam_id")
	private Exam exam;
	
	 public Long getId() {
	        return id;
	    }

	    public String getQuestionTitle() {
	        return questionTitle;
	    }

	    public void setQuestionTitle(String questionTitle) {
	        this.questionTitle = questionTitle;
	    }

	    public String getOptionA() {
	        return optionA;
	    }

	    public void setOptionA(String optionA) {
	        this.optionA = optionA;
	    }

	    public String getOptionB() {
	        return optionB;
	    }

	    public void setOptionB(String optionB) {
	        this.optionB = optionB;
	    }

	    public String getOptionC() {
	        return optionC;
	    }

	    public void setOptionC(String optionC) {
	        this.optionC = optionC;
	    }

	    public String getOptionD() {
	        return optionD;
	    }

	    public void setOptionD(String optionD) {
	        this.optionD = optionD;
	    }

	    public String getCorrectAnswer() {
	        return correctAnswer;
	    }

	    public void setCorrectAnswer(String correctAnswer) {
	        this.correctAnswer = correctAnswer;
	    }

	    public Exam getExam() {
	        return exam;
	    }

	    public void setExam(Exam exam) {
	        this.exam = exam;
	    }
	}