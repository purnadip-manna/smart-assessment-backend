package com.sas.assessment.attempt;

import com.sas.assessment.attempt.domain.AnswerSubmission;
import com.sas.assessment.attempt.domain.ExamAttempt;
import com.sas.assessment.question.Question;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerSubmissionRepository extends JpaRepository<AnswerSubmission, UUID> {

  Optional<AnswerSubmission> findByAttemptAndQuestion(ExamAttempt attempt, Question question);

  List<AnswerSubmission> findAllByAttempt(ExamAttempt attempt);

  Optional<AnswerSubmission> findByIdAndAttempt(UUID id, ExamAttempt attempt);
}
