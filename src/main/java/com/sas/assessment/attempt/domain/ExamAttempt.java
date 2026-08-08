package com.sas.assessment.attempt.domain;

import com.sas.assessment.exam.domain.Exam;
import com.sas.assessment.user.domain.User;
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
    name = "exam_attempts",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_exam_attempts_exam_student",
            columnNames = {"exam_id", "student_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private AttemptStatus status = AttemptStatus.IN_PROGRESS;

  @Column(name = "started_at")
  private OffsetDateTime startedAt;

  @Column(name = "submitted_at")
  private OffsetDateTime submittedAt;

  @Column(name = "deadline_at")
  private OffsetDateTime deadlineAt;

  /** Total points awarded; null until the attempt is fully graded. */
  @Column(name = "total_score")
  private Integer totalScore;

  /** Sum of question maxPoints, frozen when the attempt starts. */
  @Column(name = "max_score", nullable = false)
  @Builder.Default
  private int maxScore = 0;

  /** SAQ answers still awaiting an engine callback; when it reaches 0 the attempt is GRADED. */
  @Column(name = "pending_grade_count", nullable = false)
  @Builder.Default
  private int pendingGradeCount = 0;

  @Column(name = "graded_at")
  private OffsetDateTime gradedAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}
