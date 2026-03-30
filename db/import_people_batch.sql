\set ON_ERROR_STOP on

\if :{?csv_file}
\else
  \echo 'Missing required psql variable: csv_file'
  \echo 'Example: psql ... -v csv_file=/tmp/people_import.csv -f db/import_people_batch.sql'
  \quit 1
\endif

BEGIN;

SELECT CASE WHEN TO_REGCLASS('public.people') IS NOT NULL THEN 'true' ELSE 'false' END AS people_table_exists
\gset

\if :people_table_exists
\else
  \echo 'Import aborted: table "people" does not exist.'
  \echo 'Run migrations first, then retry (for example: npm run db:migrate).'
  ROLLBACK;
  DO $$ BEGIN
    RAISE EXCEPTION 'Import failed because target table people is missing';
  END $$;
\endif

CREATE TEMP TABLE people_import_raw (
  line_no BIGSERIAL PRIMARY KEY,
  name TEXT,
  position_title TEXT,
  location TEXT,
  birth_date TEXT
) ON COMMIT DROP;

\copy people_import_raw (name, position_title, location, birth_date) FROM :'csv_file' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8', NULL '')

CREATE TEMP TABLE people_import_validated AS
WITH normalized AS (
  SELECT
    line_no,
    NULLIF(BTRIM(REGEXP_REPLACE(name, E'^\uFEFF', '')), '') AS name,
    NULLIF(BTRIM(position_title), '') AS position_title,
    NULLIF(BTRIM(location), '') AS location,
    NULLIF(BTRIM(birth_date), '') AS birth_date_text
  FROM people_import_raw
),
typed AS (
  SELECT
    line_no,
    name,
    position_title,
    location,
    birth_date_text,
    CASE
      WHEN birth_date_text IS NOT NULL
        AND birth_date_text ~ '^\d{4}-\d{2}-\d{2}$'
        AND PG_INPUT_IS_VALID(birth_date_text, 'date')
        THEN birth_date_text::DATE
      ELSE NULL
    END AS birth_date_value
  FROM normalized
)
SELECT
  line_no,
  name,
  position_title,
  location,
  birth_date_text,
  birth_date_value,
  CASE
    WHEN name IS NULL THEN 'name is blank'
    WHEN position_title IS NULL THEN 'position_title is blank'
    WHEN location IS NULL THEN 'location is blank'
    WHEN birth_date_text IS NULL THEN 'birth_date is blank'
    WHEN birth_date_text !~ '^\d{4}-\d{2}-\d{2}$' THEN 'birth_date must use YYYY-MM-DD'
    WHEN NOT PG_INPUT_IS_VALID(birth_date_text, 'date')
      THEN 'birth_date is not a real calendar date'
    WHEN birth_date_value > CURRENT_DATE THEN 'birth_date cannot be in the future'
    WHEN CHAR_LENGTH(name) > 255 THEN 'name too long (max 255)'
    WHEN CHAR_LENGTH(position_title) > 120 THEN 'position_title too long (max 120)'
    WHEN CHAR_LENGTH(location) > 120 THEN 'location too long (max 120)'
    ELSE NULL
  END AS error_reason
FROM typed;

CREATE TEMP TABLE people_import_invalid AS
SELECT line_no, name, position_title, location, birth_date_text AS birth_date, error_reason
FROM people_import_validated
WHERE error_reason IS NOT NULL;

SELECT CASE WHEN COUNT(*) > 0 THEN 'true' ELSE 'false' END AS has_invalid
FROM people_import_invalid
\gset

\if :has_invalid
  \echo ''
  \echo 'Import aborted. Invalid rows found:'
  SELECT line_no, name, position_title, location, birth_date, error_reason
  FROM people_import_invalid
  ORDER BY line_no;
  ROLLBACK;
  DO $$ BEGIN
    RAISE EXCEPTION 'Import failed because CSV contains invalid rows';
  END $$;
\endif

CREATE TEMP TABLE people_import_valid AS
SELECT
  line_no,
  name,
  position_title,
  location,
  birth_date_value AS birth_date
FROM people_import_validated
WHERE error_reason IS NULL;

WITH deduped AS (
  SELECT DISTINCT ON (
      LOWER(name),
      LOWER(position_title),
      LOWER(location),
      birth_date
    )
    name,
    position_title,
    location,
    birth_date
  FROM people_import_valid
  ORDER BY
    LOWER(name),
    LOWER(position_title),
    LOWER(location),
    birth_date,
    line_no
),
inserted AS (
  INSERT INTO people (name, position_title, location, birth_date)
  SELECT d.name, d.position_title, d.location, d.birth_date
  FROM deduped d
  WHERE NOT EXISTS (
    SELECT 1
    FROM people p
    WHERE LOWER(p.name) = LOWER(d.name)
      AND LOWER(p.position_title) = LOWER(d.position_title)
      AND LOWER(p.location) = LOWER(d.location)
      AND p.birth_date = d.birth_date
  )
  RETURNING 1
)
SELECT
  (SELECT COUNT(*) FROM people_import_raw) AS source_rows,
  (SELECT COUNT(*) FROM people_import_valid) AS valid_rows,
  (SELECT COUNT(*) FROM deduped) AS distinct_valid_rows,
  (SELECT COUNT(*) FROM inserted) AS inserted_rows,
  (SELECT COUNT(*) FROM deduped) - (SELECT COUNT(*) FROM inserted) AS skipped_existing_rows;

COMMIT;
