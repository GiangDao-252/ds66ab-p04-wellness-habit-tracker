--Bảng lưu kế hoach
CREATE TABLE wellness_plan (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT wellness_plan_name_not_blank
        CHECK(btrim(name) <> '')
);