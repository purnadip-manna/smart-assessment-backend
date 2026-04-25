package com.sas.assessment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private ExamStatus status = ExamStatus.DRAFT;

  @Enumerated(EnumType.STRING)
  @Column(name = "exam_type", nullable = false, length = 10)
  private ExamType examType;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "duration_mins", nullable = false)
  private int durationMins;

  @Column(name = "open_at")
  private OffsetDateTime openAt;

  @Column(name = "close_at")
  private OffsetDateTime closeAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}

/*
 Exam:
 - id
 - status
 - examType
 - title
 - description
 - durationMins
 - openAt
 - closeAt
 - createdAt
 - updatedAt

 TODO: Need to add the following fields
 - questions (list of questions)
 - created_at (teacher)
 - knowledge_base (for SQA question's answer evaluation)
*/
