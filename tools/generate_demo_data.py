"""Generate deterministic synthetic fixtures for the existing four-table schema."""
from collections import Counter
from datetime import date, timedelta
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path
import json
import random

ROOT = Path(__file__).resolve().parents[1]
AS_OF = date(2026, 10, 23)
PREFIX = '[DEMO-W48] '
RNG = random.Random(482026)

# Values describe sample product goals, not health recommendations.
CATALOG = [
    ('Daily Walking', 'DAILY', 'steps', 7000, 0, 0, 18000),
    ('Water Intake', 'DAILY', 'liters', 2.0, 1, 0, 4.5),
    ('Sleep Duration', 'DAILY', 'hours', 8.0, 1, 4, 10),
    ('Reading Time', 'DAILY', 'minutes', 30, 0, 0, 120),
    ('Mindful Breathing', 'DAILY', 'minutes', 10, 0, 0, 35),
    ('Evening Stretching', 'DAILY', 'minutes', 15, 0, 0, 45),
    ('Weekly Exercise', 'WEEKLY', 'minutes', 150, 0, 0, 90),
    ('Outdoor Break', 'DAILY', 'minutes', 20, 0, 0, 90),
    ('Weekly Cycling', 'WEEKLY', 'minutes', 90, 0, 0, 120),
    ('Screen-Free Wind Down', 'DAILY', 'minutes', 30, 0, 0, 120),
]
LIFESTYLES = ['Campus Balance', 'Desk-to-Active', 'Morning Momentum',
              'Evening Reset', 'Weekend Explorer', 'Steady Routine',
              'Study and Recharge', 'Mindful Living']
PHASES = ['Foundation', 'Building Consistency', 'Fresh Start',
          'Small Daily Wins', 'Finding Balance', 'Long-Term Rhythm']
PROFILES = ['consistent', 'improving', 'weekend_focused',
            'interrupted', 'restarting', 'irregular']

def quote(value):
    return "'" + str(value).replace("'", "''") + "'"

def number(value, places):
    return Decimal(str(value)).quantize(Decimal(1).scaleb(-places), ROUND_HALF_UP)

def make_dataset():
    plans = []
    for index in range(48):
        profile = PROFILES[index % len(PROFILES)]
        start = AS_OF - timedelta(days=RNG.randint(95, 210))
        archived = index % 6 == 0
        end = AS_OF - timedelta(days=RNG.randint(7, 35)) if archived else AS_OF + timedelta(days=RNG.randint(14, 60))
        plan = {'name': PREFIX + LIFESTYLES[index // 6] + ' - ' + PHASES[index % 6],
                'start': start, 'end': end, 'profile': profile, 'habits': [],
                'strategy': 'STREAK' if index % 3 == 0 else 'COMPLETION_PERCENTAGE',
                'window': [7, 14, 30][index % 3]}
        selected = CATALOG[:3] + RNG.sample(CATALOG[3:], RNG.randint(3, 5))
        for hindex, template in enumerate(selected):
            name, frequency, unit, base_target, decimals, low, high = template
            target = number(base_target * RNG.choice([0.8, 1, 1, 1.2]), decimals)
            hstart = start + timedelta(days=RNG.randint(0, 14))
            hend = end
            active = not archived
            if not archived and index % 7 == 0 and hindex == len(selected) - 1:
                hend = AS_OF - timedelta(days=14)
                active = False
            habit = {'name': name, 'frequency': frequency, 'unit': unit,
                     'target': target, 'start': hstart, 'end': hend,
                     'active': active, 'rows': []}
            last = min(hend, AS_OF)
            for offset in range((last - hstart).days + 1):
                day = hstart + timedelta(days=offset)
                progress = offset / max(1, (last - hstart).days)
                weekend = day.weekday() >= 5
                probability = {'consistent': .92, 'improving': .48 + .40 * progress,
                               'weekend_focused': .91 if weekend else .59,
                               'interrupted': .80, 'restarting': .44 + .42 * progress,
                               'irregular': .57}[profile]
                # Shared interruption creates correlated gaps across a plan's habits.
                if profile == 'interrupted' and 35 <= (day - start).days <= 45:
                    continue
                if profile == 'restarting' and 20 <= (day - start).days <= 34:
                    continue
                if frequency == 'WEEKLY':
                    probability *= .68 if weekend else .43
                if RNG.random() > probability:
                    continue
                trend = .80 + .28 * progress if profile in ('improving', 'restarting') else .99
                if frequency == 'WEEKLY':
                    value = RNG.gauss(float(target) / 3.1, float(target) / 8)
                elif name == 'Sleep Duration':
                    value = RNG.gauss(float(target) - .65 + (.4 if weekend else 0), .65)
                else:
                    value = RNG.gauss(float(target) * trend * (1.12 if weekend else .96), float(target) * .29)
                # Occasional explicit zero is different from a missing check-in.
                if name != 'Sleep Duration' and RNG.random() < .014:
                    value = 0
                value = number(max(low, min(high, value)), decimals)
                if value == 0:
                    note = 'Logged zero today; planning to resume tomorrow.'
                elif name == 'Sleep Duration':
                    note = RNG.choice(['Later bedtime than planned.', 'Quiet evening and an earlier bedtime.', 'Keeping a regular bedtime routine.'])
                elif name == 'Water Intake':
                    note = RNG.choice(['Kept a bottle at the desk.', 'Tracked refills throughout the day.', 'Forgot the bottle during a busy morning.'])
                elif name == 'Daily Walking':
                    note = RNG.choice(['Walked between classes and took an evening loop.', 'Short walks between work sessions.', 'Weekend walk around the neighborhood.' if weekend else 'Walked part of the commute.'])
                elif frequency == 'WEEKLY':
                    note = RNG.choice(['Completed one session toward the weekly goal.', 'Short session today; more planned this week.', 'Longer weekend session.' if weekend else 'Session after the afternoon schedule.'])
                else:
                    note = RNG.choice(['Made time after dinner.', 'Short session during a study break.', 'Kept the session manageable today.', 'Followed the planned routine.'])
                if RNG.random() < .68 and value != 0:
                    note = ''
                habit['rows'].append((day, value, note))
            plan['habits'].append(habit)
        plans.append(plan)
    return plans

def validate(plans):
    assert len(plans) == 48 and len({p['name'] for p in plans}) == 48
    for plan in plans:
        assert len(plan['name']) <= 120
        for habit in plan['habits']:
            assert habit['target'] > 0 and habit['end'] >= habit['start']
            assert len({r[0] for r in habit['rows']}) == len(habit['rows'])
            for day, value, note in habit['rows']:
                assert habit['start'] <= day <= habit['end'] and day <= AS_OF
                assert value.is_finite() and value >= 0 and len(note) <= 1000

def write_sql(plans):
    lines = ['-- Synthetic demonstration data. Not real customer records.',
             '-- Snapshot date: 2026-10-23. Never claim these are live users.',
             '-- Requires all four tables from db/schema.sql.',
             '-- Run the entire file. Existing non-demo records are preserved.',
             'BEGIN;', "SET LOCAL search_path = public, pg_catalog;",
             "SELECT pg_advisory_xact_lock(48202648);",
             'DO $guard$ BEGIN',
             "IF CURRENT_DATE < DATE '2026-10-23' THEN RAISE EXCEPTION 'Database date precedes the fixture snapshot.'; END IF;",
             f"IF EXISTS (SELECT 1 FROM wellness_plan WHERE name LIKE {quote(PREFIX + '%')}) THEN",
             "RAISE EXCEPTION 'W48 demo data already exists. Import cancelled; no rows were added. If needed, ROLLBACK before running another script.';",
             'END IF; END $guard$;']
    for plan in plans:
        lines += ['', 'DO $plan$ DECLARE p_id BIGINT; h_id BIGINT; BEGIN',
                  'INSERT INTO wellness_plan (name, created_at)',
                  f"VALUES ({quote(plan['name'])}, TIMESTAMPTZ {quote(str(plan['start']) + ' 06:30:00+07')}) RETURNING id INTO p_id;",
                  'INSERT INTO evaluation_setting (plan_id, strategy_type, config_value)',
                  f"VALUES (p_id, {quote(plan['strategy'])}, {plan['window']});"]
        for habit in plan['habits']:
            lines += ['INSERT INTO habit (plan_id, name, frequency, target_value, unit, active_from, active_to, active)',
                      f"VALUES (p_id, {quote(habit['name'])}, {quote(habit['frequency'])}, {habit['target']}, {quote(habit['unit'])}, DATE {quote(habit['start'])}, DATE {quote(habit['end'])}, {str(habit['active']).upper()}) RETURNING id INTO h_id;"]
            rows = habit['rows']
            for first in range(0, len(rows), 150):
                batch = rows[first:first+150]
                lines.append('INSERT INTO check_in (habit_id, check_date, measurement, note) VALUES')
                lines.append(',\n'.join(f"(h_id, DATE {quote(day)}, {value}, {quote(note)})" for day, value, note in batch) + ';')
        lines += ['END $plan$;']
    lines += ['COMMIT;', '', '-- Demo-only totals:',
              f"SELECT 'plans' AS entity, count(*) AS rows FROM wellness_plan WHERE name LIKE {quote(PREFIX+'%')}",
              f"UNION ALL SELECT 'habits', count(*) FROM habit h JOIN wellness_plan p ON p.id=h.plan_id WHERE p.name LIKE {quote(PREFIX+'%')}",
              f"UNION ALL SELECT 'check_ins', count(*) FROM check_in c JOIN habit h ON h.id=c.habit_id JOIN wellness_plan p ON p.id=h.plan_id WHERE p.name LIKE {quote(PREFIX+'%')}",
              f"UNION ALL SELECT 'settings', count(*) FROM evaluation_setting s JOIN wellness_plan p ON p.id=s.plan_id WHERE p.name LIKE {quote(PREFIX+'%')};"]
    (ROOT/'db/seed_demo_large.sql').write_text('\n'.join(lines)+'\n')

if __name__ == '__main__':
    plans = make_dataset()
    validate(plans)
    write_sql(plans)
    habits = [h for p in plans for h in p['habits']]
    rows = [r for h in habits for r in h['rows']]
    summary = {'snapshot_date': str(AS_OF), 'seed': 482026, 'plans': len(plans),
               'habits': len(habits), 'check_ins': len(rows), 'settings': len(plans),
               'first_check_in': str(min(r[0] for r in rows)),
               'last_check_in': str(max(r[0] for r in rows)),
               'inactive_habits': sum(not h['active'] for h in habits),
               'weekly_habits': sum(h['frequency']=='WEEKLY' for h in habits),
               'zero_measurements': sum(r[1]==0 for r in rows),
               'nonblank_notes': sum(bool(r[2]) for r in rows),
               'behavior_profiles': dict(Counter(p['profile'] for p in plans))}
    (ROOT/'docs/demo-data-summary.json').write_text(json.dumps(summary, indent=2)+'\n')
    print(json.dumps(summary, indent=2))
