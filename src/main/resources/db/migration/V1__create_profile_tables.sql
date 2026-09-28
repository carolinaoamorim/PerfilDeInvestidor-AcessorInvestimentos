CREATE TABLE questionnaire_responses (
    id                    UUID         NOT NULL PRIMARY KEY,
    user_id               VARCHAR(255) NOT NULL,
    financial_goal        VARCHAR(50)  NOT NULL,
    risk_tolerance        VARCHAR(50)  NOT NULL,
    investment_horizon    VARCHAR(50)  NOT NULL,
    has_emergency_reserve BOOLEAN      NOT NULL,
    experience_level      VARCHAR(50)  NOT NULL,
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL,
    CONSTRAINT uk_questionnaire_responses_user_id UNIQUE (user_id)
);

CREATE TABLE investor_profiles (
    id            UUID         NOT NULL PRIMARY KEY,
    user_id       VARCHAR(255) NOT NULL,
    investor_type VARCHAR(50)  NOT NULL,
    score         INTEGER      NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    CONSTRAINT uk_investor_profiles_user_id UNIQUE (user_id)
);
