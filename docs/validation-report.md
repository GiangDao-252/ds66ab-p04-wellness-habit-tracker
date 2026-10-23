# Generation Validation

Validated before delivery:

- Exactly 48 unique plan names, each within the schema's length limit.
- Every generated habit references its containing plan via RETURNING IDs.
- Every check-in references its containing habit via RETURNING IDs.
- Every plan receives exactly one evaluation-setting row.
- Positive targets; finite, nonnegative measurements.
- Valid habit periods, no check-in outside its habit period.
- No date after the 2026-10-23 snapshot.
- No duplicate check-in dates within a habit.
- Notes within 1,000 characters.
- All three SQL files parsed using PostgreSQL's pglast parser.
- All 49 generated PL/pgSQL blocks parsed successfully.

A PostgreSQL server was not available in the generation environment.
These checks do not substitute for executing the import against your actual
database. Run verify_demo_data.sql after import and confirm the documented
counts and zero-row validation results.
