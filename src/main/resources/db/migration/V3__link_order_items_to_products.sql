ALTER TABLE order_items ADD COLUMN product_id BIGINT REFERENCES products(id);

UPDATE order_items oi
SET product_id = p.id
FROM products p
WHERE oi.product_name = p.name;

ALTER TABLE order_items ALTER COLUMN product_id SET NOT NULL;

ALTER TABLE order_items DROP COLUMN product_name;
ALTER TABLE order_items DROP COLUMN product_price;

CREATE INDEX idx_order_items_product_id ON order_items (product_id);
