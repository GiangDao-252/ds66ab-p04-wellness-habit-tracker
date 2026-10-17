-- Run after schema.sql.
BEGIN;
DO $$ 
DECLARE
    demo_plan_id BIGINT;
    water_habit_id BIGINT;
BEGIN
    -- Insert a demo wellness plan
    INSERT INTO wellness_plan (name) 
    VALUES ('Demo Wellness Plan') RETURNING id INTO demo_plan_id;

    INSERT INTO evaluation_setting (plan_id, strategy_type, config_value) 
    VALUES (demo_plan_id, 'COMPLETION_PERCENTAGE', 7);

    INSERT INTO habit (plan_id, name, frequency, target_value,
                    unit, active_from, active_to)
    VALUES (demo_plan_id, 'Drink water', 'DAILY', 2000,
        'ml', CURRENT_DATE - 7, CURRENT_DATE + 30)
    RETURNING id INTO water_habit_id;

    INSERT INTO habit (plan_id, name, frequency, target_value,
                    unit, active_from, active_to)
    VALUES (demo_plan_id, 'Walk', 'DAILY', 6000,
        'steps', CURRENT_DATE - 7, CURRENT_DATE + 30);

    INSERT INTO check_in (habit_id, check_date, measurement, note)
    VALUES
        (water_habit_id, CURRENT_DATE - 2, 2000, 'Daily target reached'),
        (water_habit_id, CURRENT_DATE - 1, 1500, 'Below daily target'),
        (water_habit_id, CURRENT_DATE, 2500, 'Daily target exceeded');
END;
$$;

COMMIT;