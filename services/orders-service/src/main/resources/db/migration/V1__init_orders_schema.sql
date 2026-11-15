CREATE TABLE orders (
    id               BIGSERIAL PRIMARY KEY,
    customer_id      VARCHAR(100)   NOT NULL,
    delivery_address VARCHAR(300)   NOT NULL,
    status           VARCHAR(30)    NOT NULL,
    total_price      NUMERIC(10, 2) NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id        BIGSERIAL PRIMARY KEY,
    order_id  BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    dish_id   BIGINT         NOT NULL,
    dish_name VARCHAR(120)   NOT NULL,
    price     NUMERIC(10, 2) NOT NULL,
    quantity  INT            NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
