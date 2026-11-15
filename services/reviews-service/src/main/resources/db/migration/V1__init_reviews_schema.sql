CREATE TABLE reviews (
    id          BIGSERIAL PRIMARY KEY,
    dish_id     BIGINT       NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    rating      INT          NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     VARCHAR(1000),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_reviews_dish_customer UNIQUE (dish_id, customer_id)
);

CREATE TABLE dish_ratings (
    dish_id        BIGINT PRIMARY KEY,
    average_rating NUMERIC(3, 2) NOT NULL DEFAULT 0.00,
    reviews_count  INT           NOT NULL DEFAULT 0
);

CREATE INDEX idx_reviews_dish_id ON reviews (dish_id);

CREATE OR REPLACE FUNCTION update_dish_rating()
RETURNS TRIGGER AS $$
DECLARE
    target_dish_id BIGINT;
BEGIN
    IF (TG_OP = 'DELETE') THEN
        target_dish_id := OLD.dish_id;
    ELSE
        target_dish_id := NEW.dish_id;
    END IF;

    INSERT INTO dish_ratings (dish_id, average_rating, reviews_count)
    SELECT
        target_dish_id,
        COALESCE(ROUND(AVG(rating)::numeric, 2), 0.00),
        COUNT(*)
    FROM reviews
    WHERE dish_id = target_dish_id
    ON CONFLICT (dish_id) DO UPDATE SET
        average_rating = EXCLUDED.average_rating,
        reviews_count  = EXCLUDED.reviews_count;

    DELETE FROM dish_ratings WHERE dish_id = target_dish_id AND reviews_count = 0;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_reviews_rating_sync
AFTER INSERT OR UPDATE OR DELETE ON reviews
FOR EACH ROW EXECUTE FUNCTION update_dish_rating();
