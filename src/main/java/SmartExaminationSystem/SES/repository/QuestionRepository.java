package SmartExaminationSystem.SES.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import SmartExaminationSystem.SES.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
	
	
	List<Question> findByExamId(Long examId);
	int countByExamId(Long examId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM Question q WHERE q.exam.id = :examId")
	void deleteByExamId(@Param("examId") Long examId);
}
