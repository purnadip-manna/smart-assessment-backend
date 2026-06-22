package com.sas.assessment.question;

import com.sas.assessment.exam.domain.Exam;
import com.sas.assessment.exam.domain.ExamType;
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
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
  private String questionText;

  @Column(name = "question_order")
  @Builder.Default
  private int questionOrder = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "question_type", nullable = false, length = 10)
  private ExamType questionType;

  @Column(name = "max_points", nullable = false)
  @Builder.Default
  private int maxPoints = 1;

  @Column(name = "option_a", columnDefinition = "TEXT")
  private String optionA;

  @Column(name = "option_b", columnDefinition = "TEXT")
  private String optionB;

  @Column(name = "option_c", columnDefinition = "TEXT")
  private String optionC;

  @Column(name = "option_d", columnDefinition = "TEXT")
  private String optionD;

  @Column(name = "correct_option", length = 1)
  private String correctOption;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}
