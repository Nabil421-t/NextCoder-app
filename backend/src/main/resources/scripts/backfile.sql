START TRANSACTION;

-- Step 1: create any patterns that don't exist yet (safe if pattern already exists)
INSERT IGNORE INTO patterns (pattern_name)
SELECT DISTINCT TRIM(pattern_name)
FROM problems
WHERE pattern_name IS NOT NULL
  AND TRIM(pattern_name) <> '';

-- Step 2: link every problem to its pattern (safe if link already exists)
INSERT IGNORE INTO problem_patterns (problem_id, pattern_id, priority)
SELECT p.id, pat.id, 1
FROM problems p
         JOIN patterns pat
              ON UPPER(TRIM(p.pattern_name)) = UPPER(pat.pattern_name)
WHERE p.pattern_name IS NOT NULL
  AND TRIM(p.pattern_name) <> '';

COMMIT;
