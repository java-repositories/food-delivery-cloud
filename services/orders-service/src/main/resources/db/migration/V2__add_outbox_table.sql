CREATE TABLE outbox_events (
                               id             BIGSERIAL PRIMARY KEY,
                               aggregate_type VARCHAR(50)  NOT NULL,
                               aggregate_id   VARCHAR(100) NOT NULL,
                               event_type     VARCHAR(50)  NOT NULL,
                               payload        TEXT         NOT NULL,
                               status         VARCHAR(20)  NOT NULL DEFAULT 'NEW',
                               created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               processed_at   TIMESTAMP
);

CREATE INDEX idx_outbox_status_created ON outbox_events (status, created_at);
