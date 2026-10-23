-- Run after importing seed_demo_large.sql. Counts are for this dataset only.
SELECT
    (SELECT count(*) FROM wellness_plan WHERE name LIKE '[DEMO-W48] %') AS plans,
    (SELECT count(*) FROM habit h JOIN wellness_plan p ON p.id=h.plan_id WHERE p.name LIKE '[DEMO-W48] %') AS habits,
    (SELECT count(*) FROM check_in c JOIN habit h ON h.id=c.habit_id JOIN wellness_plan p ON p.id=h.plan_id WHERE p.name LIKE '[DEMO-W48] %') AS check_ins,
    (SELECT count(*) FROM evaluation_setting s JOIN wellness_plan p ON p.id=s.plan_id WHERE p.name LIKE '[DEMO-W48] %') AS settings;
-- Expected: 48 | 338 | 31750 | 48

-- Expected: zero rows.
SELECT h.id, c.check_date, count(*)
FROM check_in c JOIN habit h ON h.id=c.habit_id
JOIN wellness_plan p ON p.id=h.plan_id
WHERE p.name LIKE '[DEMO-W48] %'
GROUP BY h.id, c.check_date HAVING count(*) > 1;

-- Expected: zero rows. Inactive habits may have valid historical check-ins.
SELECT c.id, h.name, c.check_date, c.measurement
FROM check_in c JOIN habit h ON h.id=c.habit_id
JOIN wellness_plan p ON p.id=h.plan_id
WHERE p.name LIKE '[DEMO-W48] %'
AND (c.check_date < h.active_from OR c.check_date > h.active_to
     OR c.check_date > CURRENT_DATE OR c.measurement < 0);

-- Weekly measurements: aggregate check-ins before comparing with a WEEKLY target.
SELECT p.name AS plan, h.name AS habit,
       date_trunc('week', c.check_date)::date AS week_start,
       sum(c.measurement) AS weekly_measurement, h.target_value, h.unit
FROM wellness_plan p JOIN habit h ON h.plan_id=p.id
JOIN check_in c ON c.habit_id=h.id
WHERE p.name LIKE '[DEMO-W48] %' AND h.frequency='WEEKLY'
GROUP BY p.name, h.id, h.name, h.target_value, h.unit,
         date_trunc('week', c.check_date)::date
ORDER BY plan, habit, week_start DESC;
