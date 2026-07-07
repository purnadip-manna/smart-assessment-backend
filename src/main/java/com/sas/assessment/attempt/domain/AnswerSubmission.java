package com.sas.assessment.attempt.domain;

import com.sas.assessment.question.Question;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "answer_submissions",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_answer_attempt_question",
            columnNames = {"attempt_id", "question_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerSubmission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attempt_id", nullable = false)
  private ExamAttempt attempt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  /** Chosen option for MCQ questions ('A'-'D'). */
  @Column(name = "selected_option", length = 1)
  private String selectedOption;

  /** Free-text answer for SAQ questions. */
  @Column(name = "answer_text", columnDefinition = "TEXT")
  private String answerText;

  /** Points awarded; null until graded. */
  @Column(name = "awarded_points")
  private Integer awardedPoints;

  @Enumerated(EnumType.STRING)
  @Column(name = "grade_status", length = 20)
  private AnswerGradeStatus gradeStatus;

  /** Grader feedback for SAQ answers. */
  @Column(columnDefinition = "TEXT")
  private String feedback;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}
