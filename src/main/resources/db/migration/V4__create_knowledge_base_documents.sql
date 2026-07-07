CREATE TABLE IF NOT EXISTS knowledge_base_documents
(
    id                UUID PRIMARY KEY       DEFAULT gen_random_uuid(),
    exam_id           UUID          NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    original_filename VARCHAR(512)  NOT NULL,
    s3_bucket         VARCHAR(255)  NOT NULL,
    s3_key            VARCHAR(1024) NOT NULL UNIQUE,
    content_type      VARCHAR(128)  NOT NULL,
    size_bytes        BIGINT        NOT NULL CHECK (size_bytes >= 0),
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                          CHECK (status IN ('PENDING', 'INGESTING', 'READY', 'FAILED')),
    error_message     TEXT,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_kb_documents_exam_id ON knowledge_base_documents (exam_id);
