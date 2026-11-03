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

CREATE TABLE check_in (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    habit_id BIGINT NOT NULL,
    check_date DATE NOT NULL,
    measurement NUMERIC NOT NULL,
    note VARCHAR(1000) NOT NULL DEFAULT '',

    CONSTRAINT check_in_habit_fk
        FOREIGN KEY (habit_id)
        REFERENCES habit (id),

    CONSTRAINT check_in_measurement_valid
        CHECK (
            measurement >= 0
            AND measurement::text NOT IN ('NaN', 'Infinity', '-Infinity')
        ),

    CONSTRAINT check_in_habit_date_unique
        UNIQUE (habit_id, check_date)
);

CREATE TABLE evaluation_setting (
    plan_id BIGINT PRIMARY KEY REFERENCES wellness_plan(id),
    strategy_type VARCHAR(30) NOT NULL
        CHECK (strategy_type IN ('COMPLETION_PERCENTAGE', 'STREAK')),
    config_value INTEGER NOT NULL CHECK (config_value > 0)
);
