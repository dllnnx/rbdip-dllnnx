CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50),
    UNIQUE (full_name)
);

INSERT INTO customers (full_name, address, phone)
SELECT DISTINCT ON (customer_full_name)
       customer_full_name,
       customer_address,
       customer_phone
FROM orders
WHERE customer_full_name IS NOT NULL
ORDER BY customer_full_name, created_at DESC;

ALTER TABLE orders ADD COLUMN customer_id BIGINT REFERENCES customers(id);

UPDATE orders o
SET customer_id = c.id
FROM customers c
WHERE o.customer_full_name = c.full_name;

ALTER TABLE orders ALTER COLUMN customer_id SET NOT NULL;

ALTER TABLE orders DROP COLUMN customer_full_name;
ALTER TABLE orders DROP COLUMN customer_address;
ALTER TABLE orders DROP COLUMN customer_phone;

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
