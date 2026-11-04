ALTER TABLE customers DROP COLUMN full_name;

ALTER TABLE customers ADD CONSTRAINT customers_first_last_name_unique UNIQUE (first_name, last_name);
