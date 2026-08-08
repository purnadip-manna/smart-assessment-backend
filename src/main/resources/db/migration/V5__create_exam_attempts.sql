CREATE TABLE IF NOT EXISTS exam_attempts
(
    id                  UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    exam_id             UUID         NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    student_id          VARCHAR(255) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status              VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS'
                            CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'GRADING', 'GRADED')),
    started_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    submitted_at        TIMESTAMPTZ,
    deadline_at         TIMESTAMPTZ,
    total_score         INTEGER,
    max_score           INTEGER      NOT NULL DEFAULT 0 CHECK (max_score >= 0),
    pending_grade_count INTEGER      NOT NULL DEFAULT 0 CHECK (pending_grade_count >= 0),
    graded_at           TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_exam_attempts_exam_student UNIQUE (exam_id, student_id)
);

CREATE INDEX idx_exam_attempts_exam_id ON exam_attempts (exam_id);
CREATE INDEX idx_exam_attempts_student_id ON exam_attempts (student_id);
