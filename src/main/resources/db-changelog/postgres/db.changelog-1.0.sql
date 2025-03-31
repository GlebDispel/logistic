-- db-changelog/postgres/db.changelog-1.0.sql
--liquibase formatted sql

--changeset gleb.chalov:1
CREATE TABLE IF NOT EXISTS deliveries
(
    id
    BIGINT
    PRIMARY
    KEY
);

--changeset gleb.chalov:2
CREATE TABLE IF NOT EXISTS delivery_status_history
(
    history_id
    BIGSERIAL
    PRIMARY
    KEY,
    delivery_id
    BIGINT
    NOT
    NULL
    REFERENCES
    deliveries
(
    id
) ON DELETE CASCADE,
    status VARCHAR
(
    50
) NOT NULL,
    status_timestamp TIMESTAMP NOT NULL
    );

CREATE INDEX idx_history_delivery_id ON delivery_status_history (delivery_id);

--changeset gleb.chalov:3
CREATE TABLE IF NOT EXISTS failed_events
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    delivery_id
    BIGINT
    NOT
    NULL,
    current_status
    VARCHAR
(
    50
),
    to_status VARCHAR
(
    50
) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    failure_timestamp TIMESTAMP NOT NULL,
    exception TEXT NOT NULL
    );