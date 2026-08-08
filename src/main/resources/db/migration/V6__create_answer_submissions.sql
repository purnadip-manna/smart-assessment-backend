CREATE TABLE IF NOT EXISTS answer_submissions
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    attempt_id      UUID         NOT NULL REFERENCES exam_attempts (id) ON DELETE CASCADE,
    question_id     UUID         NOT NULL REFERENCES questions (id) ON DELETE CASCADE,
    selected_option VARCHAR(1)            CHECK (selected_option IS NULL OR selected_option IN ('A', 'B', 'C', 'D')),
    answer_text     TEXT,
    awarded_points  INTEGER               CHECK (awarded_points IS NULL OR awarded_points >= 0),
    grade_status    VARCHAR(20)           CHECK (grade_status IS NULL OR grade_status IN ('PENDING', 'GRADED', 'FAILED')),
    feedback        TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_answer_attempt_question UNIQUE (attempt_id, question_id)
);

CREATE INDEX idx_answer_submissions_attempt_id ON answer_submissions (attempt_id);
CREATE INDEX idx_answer_submissions_question_id ON answer_submissions (question_id);
