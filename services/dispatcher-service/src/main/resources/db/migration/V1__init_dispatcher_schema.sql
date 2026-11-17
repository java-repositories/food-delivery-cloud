CREATE TABLE deliveries (
                            id               BIGSERIAL PRIMARY KEY,
                            order_id         BIGINT       NOT NULL UNIQUE,
                            customer_id      VARCHAR(100) NOT NULL,
                            delivery_address VARCHAR(300) NOT NULL,
                            courier_name     VARCHAR(100) NOT NULL,
                            status           VARCHAR(30)  NOT NULL,
                            created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
