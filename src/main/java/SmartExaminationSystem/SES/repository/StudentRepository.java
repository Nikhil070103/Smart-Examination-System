package SmartExaminationSystem.SES.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import SmartExaminationSystem.SES.model.Student;
import SmartExaminationSystem.SES.model.User;

public interface StudentRepository
extends JpaRepository<Student, Long> {

Student findByUser(User user);

		// SEARCH + FILTER
		@Query("""
		SELECT s FROM Student s
		WHERE
		(:keyword IS NULL OR
		 LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
		 LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
		 LOWER(s.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
		AND
		(:course IS NULL OR s.course = :course)
		AND
		(:semester IS NULL OR s.semester = :semester)
		""")
		
		List<Student> searchStudents(
		    String keyword,
		    String course,
		    String semester);
		
		// Dropdown values
		@Query("SELECT DISTINCT s.course FROM Student s")
		List<String> findDistinctCourses();
		
		@Query("SELECT DISTINCT s.semester FROM Student s")
		List<String> findDistinctSemesters();

		List<Student> findByFaceImageIsNull();
}