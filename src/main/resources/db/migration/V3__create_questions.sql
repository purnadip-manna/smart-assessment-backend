CREATE TABLE IF NOT EXISTS questions
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    exam_id         UUID         NOT NULL REFERENCES exams (id) ON DELETE CASCADE,
    question_text   TEXT         NOT NULL,
    question_order  INTEGER      NOT NULL DEFAULT 0,
    question_type   VARCHAR(10)  NOT NULL CHECK (question_type IN ('MCQ', 'SAQ')),
    max_points      INTEGER      NOT NULL DEFAULT 1 CHECK (max_points > 0),
    option_a        TEXT,
    option_b        TEXT,
    option_c        TEXT,
    option_d        TEXT,
    correct_option  VARCHAR(1),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_questions_exam_id ON questions (exam_id);

