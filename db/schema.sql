--Bảng lưu kế hoach
CREATE TABLE wellness_plan (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT wellness_plan_name_not_blank
        CHECK(btrim(name) <> '')
);

CREATE TABLE habit (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    frequency VARCHAR(10) NOT NULL,
    target_value NUMERIC NOT NULL,
    unit VARCHAR(40) NOT NULL,
    active_from DATE NOT NULL,
    active_to DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT habit_plan_fk
        FOREIGN KEY (plan_id)
        REFERENCES wellness_plan (id),

    CONSTRAINT habit_name_not_blank
        CHECK(btrim(name) <> ''),

    CONSTRAINT habit_frequency_check
        CHECK(frequency IN ('DAILY', 'WEEKLY')),

    CONSTRAINT habit_target_positive
        CHECK(target_value > 0 AND target_value::text NOT IN ('NaN', 'Infinity', '-Infinity')),

    CONSTRAINT habit_unit_not_blank
        CHECK(btrim(unit) <> ''),
    
    CONSTRAINT habit_active_period_valid
        CHECK (active_to >= active_from)
    );