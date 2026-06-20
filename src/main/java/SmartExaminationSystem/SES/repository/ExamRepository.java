package SmartExaminationSystem.SES.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import SmartExaminationSystem.SES.model.Exam;

public interface ExamRepository extends JpaRepository<Exam, Long> {
	
	List<Exam> findByCourseAndSemester(String course, String semester);
}
