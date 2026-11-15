CREATE TABLE dishes (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120)   NOT NULL,
    description VARCHAR(500),
    price       NUMERIC(10, 2) NOT NULL CHECK (price > 0),
    category    VARCHAR(50)    NOT NULL,
    available   BOOLEAN        NOT NULL DEFAULT TRUE,
    image_url   VARCHAR(255),
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dishes_category ON dishes (category);
CREATE INDEX idx_dishes_available ON dishes (available);
