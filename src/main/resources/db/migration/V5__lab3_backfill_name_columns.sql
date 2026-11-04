UPDATE customers
SET first_name = split_part(full_name, ' ', 1),
    last_name = CASE
        WHEN position(' ' IN full_name) > 0
        THEN substring(full_name FROM position(' ' IN full_name) + 1)
        ELSE ''
    END
WHERE full_name IS NOT NULL;

ALTER TABLE customers ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE customers ALTER COLUMN last_name SET NOT NULL;
