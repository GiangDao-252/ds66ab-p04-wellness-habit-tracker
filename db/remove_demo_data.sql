-- Explicit reset: removes ONLY plans with the reserved [DEMO-W48] prefix
-- and all their dependent data, including edits made to those demo plans.
-- Does not reset identity sequences or delete non-demo plans.
BEGIN;
SET LOCAL search_path = public, pg_catalog;
SELECT pg_advisory_xact_lock(48202648);
DELETE FROM check_in WHERE habit_id IN (
    SELECT h.id FROM habit h JOIN wellness_plan p ON p.id=h.plan_id
    WHERE p.name LIKE '[DEMO-W48] %'
);
DELETE FROM habit WHERE plan_id IN (
    SELECT id FROM wellness_plan WHERE name LIKE '[DEMO-W48] %'
);
DELETE FROM evaluation_setting WHERE plan_id IN (
    SELECT id FROM wellness_plan WHERE name LIKE '[DEMO-W48] %'
);
DELETE FROM wellness_plan WHERE name LIKE '[DEMO-W48] %';
COMMIT;
