package SmartExaminationSystem.SES.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import SmartExaminationSystem.SES.model.Exam;
import SmartExaminationSystem.SES.model.Result;
import SmartExaminationSystem.SES.model.Student;

public interface ResultRepository extends JpaRepository<Result, Long> {
	
	//List<Result> findAllByOrderBySubmittedAtDesc();
	List<Result> findByStudent(Student student);

	 boolean existsByStudentAndExam(
	            Student student,
	            Exam exam
	    );
	 
	 @Query("SELECT MAX(r.score) FROM Result r")
	 Integer findHighestMarks();

	 List<Result> findByExamIdOrderByScoreDesc(Long examId);

	long countByExam_Id(Long id);

	List<Result> findTop10ByOrderByScoreDesc();
	 }