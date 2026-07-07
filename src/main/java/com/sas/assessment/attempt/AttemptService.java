package com.sas.assessment.attempt;

import com.sas.assessment.attempt.domain.AnswerGradeStatus;
import com.sas.assessment.attempt.domain.AnswerSubmission;
import com.sas.assessment.attempt.domain.AttemptStatus;
import com.sas.assessment.attempt.domain.ExamAttempt;
import com.sas.assessment.attempt.dto.AnswerGradingRequestedEvent;
import com.sas.assessment.attempt.dto.AnswerResultResponse;
import com.sas.assessment.attempt.dto.AttemptResultResponse;
import com.sas.assessment.attempt.dto.AttemptStateResponse;
import com.sas.assessment.attempt.dto.GradeAnswerCallbackRequest;
import com.sas.assessment.attempt.dto.SaveAnswerRequest;
import com.sas.assessment.attempt.dto.SubmitAttemptResponse;
import com.sas.assessment.config.AwsProperties;
import com.sas.assessment.exam.ExamService;
import com.sas.assessment.exam.domain.Exam;
import com.sas.assessment.exam.domain.ExamStatus;
import com.sas.assessment.exam.domain.ExamType;
import com.sas.assessment.exception.BadRequestException;
import com.sas.assessment.exception.ResourceNotFoundException;
import com.sas.assessment.question.Question;
import com.sas.assessment.question.QuestionRepository;
import com.sas.assessment.question.dto.QuestionResponse;
import com.sas.assessment.user.domain.User;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AttemptService {

  private static final Set<String> VALID_OPTIONS = Set.of("A", "B", "C", "D");

  private final ExamAttemptRepository attemptRepository;
  private final AnswerSubmissionRepository answerRepository;
  private final QuestionRepository questionRepository;
  private final ExamService examService;
  private final ApplicationEventPublisher eventPublisher;
  private final AwsProperties awsProperties;

  public AttemptService(
      ExamAttemptRepository attemptRepository,
      AnswerSubmissionRepository answerRepository,
      QuestionRepository questionRepository,
      ExamService examService,
      ApplicationEventPublisher eventPublisher,
      AwsProperties awsProperties) {
    this.attemptRepository = attemptRepository;
    this.answerRepository = answerRepository;
    this.questionRepository = questionRepository;
    this.examService = examService;
    this.eventPublisher = eventPublisher;
    this.awsProperties = awsProperties;
  }

  /** Starts a new attempt or resumes the student's existing in-progress one. */
  @Transactional
  public AttemptStateResponse start(UUID examId, User student) {
    Exam exam = examService.findById(examId);
    OffsetDateTime now = OffsetDateTime.now();
    verifyExamOpen(exam, now);

    ExamAttempt attempt =
        attemptRepository
            .findByExamAndStudent(exam, student)
            .map(existing -> resumeOrReject(existing, now))
            .orElseGet(() -> createAttempt(exam, student, now));

    return buildState(attempt);
  }

  /** Current student's attempt state (questions + saved answers); lazily auto-submits if expired. */
  @Transactional
  public AttemptStateResponse getState(UUID examId, User student) {
    Exam exam = examService.findById(examId);
    ExamAttempt attempt =
        attemptRepository
            .findByExamAndStudent(exam, student)
            .orElseThrow(() -> new ResourceNotFoundException("No attempt found for this exam"));
    autoSubmitIfExpired(attempt, OffsetDateTime.now());
    return buildState(attempt);
  }

  /** Upserts a single answer in an in-progress attempt. */
  @Transactional
  public AnswerResultResponse saveAnswer(UUID attemptId, SaveAnswerRequest request, User student) {
    ExamAttempt attempt = findOwnedAttempt(attemptId, student);
    requireInProgress(attempt);
    if (attempt.getDeadlineAt() != null && OffsetDateTime.now().isAfter(attempt.getDeadlineAt())) {
      throw new BadRequestException("Attempt deadline has passed; please submit");
    }

    Question question =
        questionRepository
            .findById(request.questionId())
            .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
    if (!question.getExam().getId().equals(attempt.getExam().getId())) {
      throw new BadRequestException("Question does not belong to this exam");
    }

    AnswerSubmission answer =
        answerRepository
            .findByAttemptAndQuestion(attempt, question)
            .orElseGet(
                () -> AnswerSubmission.builder().attempt(attempt).question(question).build());

    if (attempt.getExam().getExamType() == ExamType.MCQ) {
      String option = normalizeOption(request.selectedOption());
      answer.setSelectedOption(option);
      answer.setAnswerText(null);
    } else {
      answer.setAnswerText(request.answerText());
      answer.setSelectedOption(null);
    }

    return AnswerResultResponse.forState(answerRepository.save(answer));
  }

  /** Finalizes the attempt: MCQ graded inline, SAQ dispatched to the evaluation engine. */
  @Transactional
  public SubmitAttemptResponse submit(UUID attemptId, User student) {
    ExamAttempt attempt = findOwnedAttempt(attemptId, student);
    requireInProgress(attempt);
    finalizeAttempt(attempt, OffsetDateTime.now());
    return SubmitAttemptResponse.from(attempt);
  }

  @Transactional(readOnly = true)
  public AttemptResultResponse getResult(UUID attemptId, User student) {
    ExamAttempt attempt = findOwnedAttempt(attemptId, student);
    List<AnswerResultResponse> answers =
        answerRepository.findAllByAttempt(attempt).stream()
            .map(AnswerResultResponse::forResult)
            .toList();
    return AttemptResultResponse.of(attempt, answers);
  }

  /** Applies a per-answer grade from the evaluation engine. Idempotent and forward-only. */
  @Transactional
  public void applyGradeCallback(
      UUID attemptId, UUID answerId, GradeAnswerCallbackRequest request) {
    ExamAttempt attempt =
        attemptRepository
            .findByIdForUpdate(attemptId)
            .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
    AnswerSubmission answer =
        answerRepository
            .findByIdAndAttempt(answerId, attempt)
            .orElseThrow(() -> new ResourceNotFoundException("Answer not found for this attempt"));

    if (isTerminal(answer.getGradeStatus())) {
      return; // idempotent re-delivery
    }

    AnswerGradeStatus newStatus = request.status();
    if (newStatus == AnswerGradeStatus.GRADED) {
      if (request.score() == null) {
        throw new BadRequestException("score is required when status is GRADED");
      }
      int max = answer.getQuestion().getMaxPoints();
      answer.setAwardedPoints(Math.max(0, Math.min(request.score(), max)));
    } else {
      answer.setAwardedPoints(0);
    }
    answer.setFeedback(request.feedback());
    answer.setGradeStatus(newStatus);
    answerRepository.save(answer);

    attempt.setPendingGradeCount(attempt.getPendingGradeCount() - 1);
    if (attempt.getPendingGradeCount() <= 0) {
      attempt.setTotalScore(sumAwardedPoints(attempt));
      attempt.setStatus(AttemptStatus.GRADED);
      attempt.setGradedAt(OffsetDateTime.now());
    }
    attemptRepository.save(attempt);
    log.info("Graded answer {} ({}); attempt {} pending={}",
        answerId, newStatus, attemptId, attempt.getPendingGradeCount());
  }

  // ---- internal helpers ----

  private void verifyExamOpen(Exam exam, OffsetDateTime now) {
    if (exam.getStatus() != ExamStatus.OPEN) {
      throw new BadRequestException("Exam is not open for attempts");
    }
    if (exam.getOpenAt() != null && now.isBefore(exam.getOpenAt())) {
      throw new BadRequestException("Exam has not opened yet");
    }
    if (exam.getCloseAt() != null && now.isAfter(exam.getCloseAt())) {
      throw new BadRequestException("Exam has closed");
    }
  }

  private ExamAttempt resumeOrReject(ExamAttempt existing, OffsetDateTime now) {
    if (existing.getStatus() != AttemptStatus.IN_PROGRESS) {
      throw new BadRequestException("You have already submitted this exam");
    }
    autoSubmitIfExpired(existing, now);
    return existing;
  }

  private ExamAttempt createAttempt(Exam exam, User student, OffsetDateTime now) {
    List<Question> questions = questionRepository.findAllByExamOrderByQuestionOrder(exam);
    if (questions.isEmpty()) {
      throw new BadRequestException("Exam has no questions");
    }
    int maxScore = questions.stream().mapToInt(Question::getMaxPoints).sum();

    OffsetDateTime deadline = now.plusMinutes(exam.getDurationMins());
    if (exam.getCloseAt() != null && deadline.isAfter(exam.getCloseAt())) {
      deadline = exam.getCloseAt();
    }

    return attemptRepository.save(
        ExamAttempt.builder()
            .exam(exam)
            .student(student)
            .status(AttemptStatus.IN_PROGRESS)
            .startedAt(now)
            .deadlineAt(deadline)
            .maxScore(maxScore)
            .pendingGradeCount(0)
            .build());
  }

  private void autoSubmitIfExpired(ExamAttempt attempt, OffsetDateTime now) {
    if (attempt.getStatus() == AttemptStatus.IN_PROGRESS
        && attempt.getDeadlineAt() != null
        && now.isAfter(attempt.getDeadlineAt())) {
      log.info("Attempt {} expired; auto-submitting", attempt.getId());
      finalizeAttempt(attempt, now);
    }
  }

  private void finalizeAttempt(ExamAttempt attempt, OffsetDateTime now) {
    attempt.setSubmittedAt(now);
    Exam exam = attempt.getExam();
    List<Question> questions = questionRepository.findAllByExamOrderByQuestionOrder(exam);

    if (exam.getExamType() == ExamType.MCQ) {
      gradeMcq(attempt, questions, now);
    } else {
      dispatchSaq(attempt, now);
    }
    attemptRepository.save(attempt);
  }

  private void gradeMcq(ExamAttempt attempt, List<Question> questions, OffsetDateTime now) {
    int total = 0;
    for (Question question : questions) {
      AnswerSubmission answer =
          answerRepository.findByAttemptAndQuestion(attempt, question).orElse(null);
      if (answer == null) {
        continue; // unanswered → 0 points
      }
      int awarded =
          answer.getSelectedOption() != null
                  && answer.getSelectedOption().equalsIgnoreCase(question.getCorrectOption())
              ? question.getMaxPoints()
              : 0;
      answer.setAwardedPoints(awarded);
      answer.setGradeStatus(AnswerGradeStatus.GRADED);
      answerRepository.save(answer);
      total += awarded;
    }
    attempt.setTotalScore(total);
    attempt.setStatus(AttemptStatus.GRADED);
    attempt.setGradedAt(now);
    attempt.setPendingGradeCount(0);
  }

  private void dispatchSaq(ExamAttempt attempt, OffsetDateTime now) {
    List<AnswerSubmission> answers = answerRepository.findAllByAttempt(attempt);
    int localTotal = 0;
    int pending = 0;

    for (AnswerSubmission answer : answers) {
      if (isBlank(answer.getAnswerText())) {
        answer.setAwardedPoints(0); // blank → graded 0 locally, no engine call
        answer.setGradeStatus(AnswerGradeStatus.GRADED);
        answerRepository.save(answer);
      } else {
        answer.setGradeStatus(AnswerGradeStatus.PENDING);
        AnswerSubmission saved = answerRepository.save(answer);
        pending++;
        eventPublisher.publishEvent(
            AnswerGradingRequestedEvent.of(
                attempt.getId(),
                saved.getId(),
                saved.getQuestion().getId(),
                attempt.getExam().getId(),
                saved.getQuestion().getQuestionText(),
                saved.getAnswerText(),
                saved.getQuestion().getMaxPoints(),
                buildCallbackUrl(attempt.getId(), saved.getId()),
                now));
      }
    }

    attempt.setPendingGradeCount(pending);
    if (pending == 0) {
      attempt.setTotalScore(localTotal);
      attempt.setStatus(AttemptStatus.GRADED);
      attempt.setGradedAt(now);
    } else {
      attempt.setStatus(AttemptStatus.GRADING);
    }
  }

  private int sumAwardedPoints(ExamAttempt attempt) {
    return answerRepository.findAllByAttempt(attempt).stream()
        .map(AnswerSubmission::getAwardedPoints)
        .filter(p -> p != null)
        .mapToInt(Integer::intValue)
        .sum();
  }

  private AttemptStateResponse buildState(ExamAttempt attempt) {
    List<QuestionResponse> questions =
        questionRepository.findAllByExamOrderByQuestionOrder(attempt.getExam()).stream()
            .map(QuestionResponse::fromForStudent)
            .toList();
    List<AnswerResultResponse> answers =
        answerRepository.findAllByAttempt(attempt).stream()
            .map(AnswerResultResponse::forState)
            .toList();
    return AttemptStateResponse.of(attempt, questions, answers);
  }

  private ExamAttempt findOwnedAttempt(UUID attemptId, User student) {
    return attemptRepository
        .findByIdAndStudent(attemptId, student)
        .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
  }

  private void requireInProgress(ExamAttempt attempt) {
    if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
      throw new BadRequestException("Attempt is not in progress");
    }
  }

  private String normalizeOption(String selectedOption) {
    if (selectedOption == null) {
      return null;
    }
    String option = selectedOption.trim().toUpperCase();
    if (!VALID_OPTIONS.contains(option)) {
      throw new BadRequestException("selectedOption must be one of A, B, C, D");
    }
    return option;
  }

  private boolean isTerminal(AnswerGradeStatus status) {
    return status == AnswerGradeStatus.GRADED || status == AnswerGradeStatus.FAILED;
  }

  private boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private String buildCallbackUrl(UUID attemptId, UUID answerId) {
    String base = awsProperties.getKnowledgeBase().getCallbackBaseUrl();
    if (base == null) {
      base = "";
    }
    String trimmed = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    return trimmed + "/api/v1/internal/attempts/" + attemptId + "/answers/" + answerId + "/grade";
  }
}
