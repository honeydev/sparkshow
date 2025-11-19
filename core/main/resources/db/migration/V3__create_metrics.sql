CREATE TABLE metrics (
    id SERIAL PRIMARY KEY,
    query_id INT NOT NULL,
    value jsonb NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_query_id
        FOREIGN KEY(query_id)
            REFERENCES queries(id)
            ON DELETE CASCADE
);
