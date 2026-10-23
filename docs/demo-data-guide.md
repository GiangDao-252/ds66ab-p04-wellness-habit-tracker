# Wellness Demonstration Dataset

This is synthetic product demonstration data, not real customers, measured
engagement, clinical evidence, or a statement that the application is ready
for production. Goals are example settings, not medical recommendations.

## Contents

| Entity | Records |
| --- | ---: |
| Wellness plans | 48 |
| Habits | 338 |
| Check-ins | 31,750 |
| Evaluation settings | 48 |

Snapshot: 2026-10-23. Check-in dates: 2026-03-29 through 2026-10-23.
The data is a fixed, reproducible snapshot; dates do not advance when imported.
If presenting much later, regenerate deliberately with an updated AS_OF date
and verify the new totals rather than treating these as recent live records.

## Behavior and scenarios

- Eight lifestyle themes, each with six differently patterned plans.
- Consistent, improving, weekend-focused, interrupted, restarting,
  and irregular logging patterns; eight plans per profile.
- Different habit start dates, targets, plan lengths, and recording rates.
- Walking, water, sleep, reading, breathing, stretching, exercise,
  outdoor breaks, cycling, and screen-free time.
- 56 weekly habits; their entries record individual sessions, not weekly totals.
- 62 inactive habits with historical records.
- 389 explicit zero measurements, distinct from days without a check-in.
- 10,339 nonblank notes; most other entries have an empty note.
- Both supported strategies and reporting windows of 7, 14, or 30 days.

The schema has no user/account table. These are 48 plans, NOT 48 users.
No names, email addresses, passwords, or real health records are generated.

## Import using pgAdmin

1. Ensure all four existing project tables are installed in `public`:
   `wellness_plan`, `habit`, `check_in`, and `evaluation_setting`.
   On an empty database, run the corrected project `db/schema.sql` first.
2. Choose the target database, open Query Tool, and open
   `db/seed_demo_large.sql` using the file-open button.
3. Execute the entire file, without selecting just part of it.
4. Check the final counts, then run `db/verify_demo_data.sql`.
5. Run the existing PersistenceMain console and choose List wellness plans.
   Use returned IDs rather than assuming a particular ID range.

The importer uses generated IDs returned by PostgreSQL and a transaction.
It does not truncate tables, reset sequences, or overwrite existing rows.
Each added plan has the reserved prefix `[DEMO-W48] `.
Do not remove that prefix if you want the supplied reset and verification
scripts to continue identifying that plan.

Importing twice is blocked if any matching demo plan already exists.
If pgAdmin leaves a failed transaction open, execute `ROLLBACK;` before
trying another operation. A database clock earlier than the snapshot date
is also rejected to prevent future check-ins.

## Reset intentionally

Run `db/remove_demo_data.sql` only when you want to remove this demo dataset.
It removes all records under matching demo plans, including any demo edits.
It leaves plans without the reserved prefix untouched.
Keep the marker reserved for this dataset; do not use it for personal plans.

## Reproduce

From the project root:

```bash
python3 tools/generate_demo_data.py
```

Requires Python 3 with only the standard library. Random seed: 482026.
The generator validates its records before writing SQL and prints a summary.
Changing AS_OF, the seed, or generation logic may change counts and dates;
update the expected counts in the verification SQL and documentation then.

## Demonstration flow

1. Open one plan and browse its habits.
2. Show a daily measurement history with both missing and zero days.
3. Compare an improving pattern with an interrupted pattern.
4. Sum session measurements by calendar week for a weekly habit.
5. Browse historical entries for an inactive habit.
6. Add or edit an entry within an active habit's period.
7. Attempt to create a second check-in on an existing date to show validation.
8. Restart the application and read back stored data.

The existing console must be used with the generated IDs. Charts and user
accounts are not added by this SQL dataset. The current schema also has no
timestamp or revision history for individual check-ins and no historical
target-version table; these fixtures do not invent those features.
