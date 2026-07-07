package com.sas.assessment.knowledgebase;

import com.sas.assessment.exam.domain.Exam;
import com.sas.assessment.knowledgebase.domain.KnowledgeBaseDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBaseDocument, UUID> {
  List<KnowledgeBaseDocument> findAllByExamOrderByCreatedAt(Exam exam);
}
