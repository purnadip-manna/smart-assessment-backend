package com.sas.assessment.question;

import com.sas.assessment.exam.domain.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
  List<Question> findAllByExamOrderByQuestionOrder(Exam exam);

  long countByExam(Exam exam);
}
