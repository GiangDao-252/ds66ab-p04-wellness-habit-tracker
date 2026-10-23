# Database and Persistence Guide

## 1. Purpose

The persistence layer stores wellness plans, habits, check-ins,
and evaluation settings in PostgreSQL.

Repositories execute SQL using a connection supplied by the caller.
Services manage transactions and enforce application rules.

## 2. Requirements

- A Java Development Kit supporting records and text blocks.
- PostgreSQL.
- The PostgreSQL JDBC driver placed in the `lib` directory.
- A database named `wellness_g4`.

The project uses `src` as its Java source directory and
`build/classes` as its compilation output directory.

## 3. Database Setup

Create the `wellness_g4` database in PostgreSQL.

Connect to that database and execute `db/schema.sql`.

For a new database, create tables in this dependency order:

1. `wellness_plan`
2. `habit`
3. `check_in`
4. `evaluation_setting`

The schema script is intended for initial setup.
Running it again against an existing schema may report that tables
already exist.

## 4. Connection Configuration

The application reads these environment variables:

| Variable               | Purpose                        |
| ---------------------- | ------------------------------ |
| `WELLNESS_DB_URL`      | PostgreSQL JDBC connection URL |
| `WELLNESS_DB_USER`     | PostgreSQL login role          |
| `WELLNESS_DB_PASSWORD` | Password for that role         |

Example configuration for a local `.env` file:

```bash
export WELLNESS_DB_URL='jdbc:postgresql://localhost:5432/wellness_g4'
export WELLNESS_DB_USER='postgres'
export WELLNESS_DB_PASSWORD='replace_with_your_local_password'
```

Load the configuration in the same terminal used to run Java:

```bash
source .env
```

Java does not load `.env` automatically.
The file must remain excluded from Git.

## 5. Database Tables

| Table                | Stored data                                                       |
| -------------------- | ----------------------------------------------------------------- |
| `wellness_plan`      | Plan name and creation timestamp                                  |
| `habit`              | Plan reference, frequency, target, unit, active dates, and status |
| `check_in`           | Habit reference, date, measurement, and note                      |
| `evaluation_setting` | Strategy type and configuration value for a plan                  |

Relationships:

- One plan can contain multiple habits.
- One habit can contain multiple check-ins.
- A plan can have one evaluation-setting row.

The primary key of `evaluation_setting` is also a foreign key
referencing `wellness_plan`.

## 6. Database Constraints

The database enforces:

- Valid foreign-key references.
- Nonblank plan names, habit names, and units.
- Supported habit frequencies: `DAILY` and `WEEKLY`.
- Positive, finite habit targets.
- Nonnegative, finite check-in measurements.
- A valid habit date range.
- At most one check-in per habit per date.
- Supported evaluation strategies.
- A positive evaluation configuration value.

Deleting a parent row with dependent rows is blocked by foreign keys.
The schema does not automatically cascade these deletions.

## 7. Main Components

| Component                     | Responsibility                                     |
| ----------------------------- | -------------------------------------------------- |
| `ConnectionFactory`           | Open a JDBC connection using environment variables |
| `Transactions`                | Commit successful work and roll back failed work   |
| `WellnessPlanRepository`      | Read and modify plan rows                          |
| `HabitRepository`             | Read and modify habit rows                         |
| `CheckInRepository`           | Read and modify check-in rows                      |
| `EvaluationSettingRepository` | Read and save evaluation settings                  |
| `WellnessPlanService`         | Create a plan and its default settings atomically  |
| `CheckInService`              | Validate and transact check-in operations          |

Repository methods do not commit, roll back, or close the
connection supplied by the caller.

They close their own statements and result sets.

## 8. Service Entry Points

Create a plan with default evaluation settings:

```java
long planId = new WellnessPlanService().createPlan("My Wellness Plan");
```

Add a check-in:

```java
CheckInService service = new CheckInService();

long checkInId = service.add(
        habitId,
        LocalDate.now(),
        new BigDecimal("3500"),
        "Evening walk"
);
```

Update an existing check-in:

```java
service.update(new CheckInRow(
        checkInId,
        habitId,
        LocalDate.now(),
        new BigDecimal("4200"),
        "Updated measurement"
));
```

Delete a check-in:

```java
service.delete(checkInId, habitId);
```

These examples require the corresponding project imports,
`java.time.LocalDate`, and `java.math.BigDecimal`.

## 9. Check-In Rules

Adding or updating a check-in requires:

- An existing, active habit.
- A date within the habit's active period.
- A date that is not in the future.
- A nonnegative measurement.
- A note containing at most 1000 characters.
- No other check-in for the same habit and date.

Updates and deletions identify a check-in using both its ID
and its habit ID.

Deleting an existing check-in is permitted when its habit is inactive.

The service locks the selected habit row during check-in mutations.
The database unique constraint prevents duplicate daily check-ins.

Application rules must be accessed through the service layer.
Direct repository calls do not perform every service-level validation.

## 10. Error Handling

`WellnessException` reports application rule violations.

`SQLException` reports database failures that are not translated
into an application error.

The user interface should display a suitable message when an
operation fails and refresh displayed data after a successful operation.

Database calls should run outside the Swing Event Dispatch Thread,
for example using `SwingWorker`.

## 11. Running Check-In Service Tests

After compiling the test classes, load the configuration and run:

```bash
source .env

java -cp "build/classes:lib/*" \
  com.oop.project.service.CheckInServiceTest

java -cp "build/classes:lib/*" \
  com.oop.project.service.CheckInServiceUpdateTest

java -cp "build/classes:lib/*" \
  com.oop.project.service.CheckInServiceDeleteTest
```

These commands use the macOS/Linux classpath separator.

The tests require a running PostgreSQL database with the schema installed.
They create their own fixtures and attempt to remove those fixtures
when testing finishes.
