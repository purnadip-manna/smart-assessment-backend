CREATE TABLE IF NOT EXISTS exams
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED')),
    exam_type     VARCHAR(10)  NOT NULL CHECK (exam_type IN ('MCQ', 'SAQ')),
    title         VARCHAR(255) NOT NULL,
    description   TEXT,
    duration_mins INTEGER      NOT NULL CHECK (duration_mins > 0),
    open_at       TIMESTAMPTZ,
    close_at      TIMESTAMPTZ,
    created_by    VARCHAR(255) NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);