CREATE TABLE IF NOT EXISTS users
(
    id             VARCHAR(255) PRIMARY KEY NOT NULL,
    email          VARCHAR(255) UNIQUE      NOT NULL,
    name           VARCHAR(255)             NOT NULL,
    nickname       VARCHAR(255)             NOT NULL,
    role           VARCHAR(20)              NOT NULL CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT')),
    email_verified BOOLEAN                  NOT NULL DEFAULT FALSE,
    active         BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ              NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ              NOT NULL DEFAULT now()
);
