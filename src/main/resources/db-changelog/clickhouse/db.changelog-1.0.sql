--liquibase formatted sql

--changeset gleb.chalov:1
CREATE TABLE IF NOT EXISTS delivery_status_history
(
    historyId         UInt64,
    delivery_id       UInt64,
    status            String,
    statusTimestamp   DateTime DEFAULT now(),
    slaViolated       UInt8,         -- 0 = false, 1 = true
    violationDuration Int64          -- Длительность в секундах
    )
    ENGINE = MergeTree()
    ORDER BY (historyId, statusTimestamp)  -- Оптимизировано для типичных запросов
    SETTINGS index_granularity = 8192;

--rollback DROP TABLE delivery_status_history;