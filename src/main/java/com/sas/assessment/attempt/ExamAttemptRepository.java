package com.sas.assessment.attempt;

import com.sas.assessment.attempt.domain.ExamAttempt;
import com.sas.assessment.exam.domain.Exam;
import com.sas.assessment.user.domain.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, UUID> {

  Optional<ExamAttempt> findByExamAndStudent(Exam exam, User student);

  boolean existsByExamAndStudent(Exam exam, User student);

  Optional<ExamAttempt> findByIdAndStudent(UUID id, User student);

  /** Pessimistic lock so concurrent grade callbacks serialize the pending-count decrement. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from ExamAttempt a where a.id = :id")
  Optional<ExamAttempt> findByIdForUpdate(@Param("id") UUID id);
}
