# Database Design — G4 Wellness Habit Tracker

## 1. Purpose

Store wellness plans, habits, daily check-ins and evaluation settings
so that the application can restore its data after restarting.

## 2. Tables

| Table              | Purpose                                                      |
| ------------------ | ------------------------------------------------------------ |
| wellness_plan      | Store each wellness plan                                     |
| habit              | Store habits, targets, units, frequency and active periods   |
| check_in           | Store daily measurements for each habit                      |
| evaluation_setting | Store the selected evaluation strategy and its configuration |

## 3. Relationships

- One wellness plan contains many habits.
- Each habit belongs to exactly one wellness plan.
- One habit has many daily check-ins.
- Each check-in belongs to exactly one habit.
- Each wellness plan has one current evaluation setting.

## 4. Main Fields

### wellness_plan

- id: unique plan identifier.
- name: plan name.
- created_at: creation timestamp.

### habit

- id: unique habit identifier.
- plan_id: reference to the owning plan.
- name: habit name.
- frequency: habit frequency.
- target_value: target measurement.
- unit: measurement unit.
- active_from: first day of the active period.
- active_to: last day of the active period.
- active: whether the habit is currently enabled.

### check_in

- id: unique check-in identifier.
- habit_id: reference to the habit.
- check_date: date of the measurement.
- measurement: actual measured value.
- note: optional user note.

### evaluation_setting

- plan_id: identifies the plan and its single current setting.
- strategy_type: selected evaluation strategy.
- config_value: number of days in the report window.

## 5. Proposed Data Rules

- A habit target must be greater than zero.
- A check-in measurement must be greater than or equal to zero.
- The active period must end on or after its start date.
- A check-in date must fall within the habit's active period.
- Each habit can have at most one check-in per date.
- Habits must reference an existing plan.
- Check-ins must reference an existing habit.
- Each plan can have at most one current evaluation setting.

Frequency values and evaluation configuration must be agreed with
the domain implementation before integration.
