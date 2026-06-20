package SmartExaminationSystem.SES.controller;

import SmartExaminationSystem.SES.model.*;
import SmartExaminationSystem.SES.repository.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; 

@Controller
@RequestMapping("/admin")
public class QuestionController {

	@Autowired
	private ExamRepository examRepository;
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@GetMapping("/questions")
	public String selectExamForQuestions(Model model) {
	    model.addAttribute("exams", examRepository.findAll());
	    return "select-exam";
	}
	
	//Show Add Question Form 
	@GetMapping("/{id}/questions/add")
	public String showAddQuestionForm(@PathVariable Long id, Model model) {

	    Exam exam = examRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Exam not found"));

	    int questionCount = questionRepository.countByExamId(id);

	    model.addAttribute("question", new Question());
	    model.addAttribute("examId", id);
	    model.addAttribute("exam", exam);
	    model.addAttribute("questionCount", questionCount);

	    return "add-question";
	}
	
	//Save Question
	 @PostMapping("/{id}/questions/add")
	public String saveQuestion(@PathVariable Long id,
			                   @ModelAttribute Question question) {
		
		Exam exam = examRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Exam not found "));
		
		question.setExam(exam);
		
		questionRepository.save(question);
		
		return "redirect:/admin/" + id + "/questions/add";
	}
	
	 //View Question
	 @GetMapping("/{id}/questions")
	 public String viewQuestions(@PathVariable Long id, Model model) {

		 List<Question> questions = questionRepository.findByExamId(id);

	     model.addAttribute("questions", questions);
	     model.addAttribute("examId", id);

	     return "view-questions";
	 }
	 
	 //Delete Question
	 @GetMapping("/questions/delete/{qid}/{eid}")
	 public String deleteQuestion(@PathVariable Long qid,
	                              @PathVariable Long eid) {

	     questionRepository.deleteById(qid);

	     return "redirect:/admin/" + eid + "/questions";
	 }
	 
	 //Edit Question
	 @GetMapping("/questions/edit/{qid}/{eid}")
	 public String editQuestionForm(@PathVariable Long qid,
	                               @PathVariable Long eid,
	                               Model model) {

	     Question question = questionRepository.findById(qid)
	             .orElseThrow(() -> new RuntimeException("Question not found"));

	     model.addAttribute("question", question);
	     model.addAttribute("examId", eid);

	     return "edit-question";
	 }
	 
	 //Update Question
	 @PostMapping("/questions/update/{qid}/{eid}")
	 public String updateQuestion(@PathVariable Long qid,
	                              @PathVariable Long eid,
	                              @ModelAttribute Question question) {

	     Question existing = questionRepository.findById(qid)
	             .orElseThrow(() -> new RuntimeException("Question not found"));

	     existing.setQuestionTitle(question.getQuestionTitle());
	     existing.setOptionA(question.getOptionA());
	     existing.setOptionB(question.getOptionB());
	     existing.setOptionC(question.getOptionC());
	     existing.setOptionD(question.getOptionD());
	     existing.setCorrectAnswer(question.getCorrectAnswer());

	     questionRepository.save(existing);

	     return "redirect:/admin/" + eid + "/questions";
	 }
	
}
