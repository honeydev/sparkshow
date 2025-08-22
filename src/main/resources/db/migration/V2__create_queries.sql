CREATE TYPE query_state AS ENUM ('new', 'running', 'enqueued', 'waiting_retry', 'finished',
    'failed');

CREATE TABLE sources (
  id SERIAL PRIMARY KEY,
  path VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  header BOOLEAN,
  delimiter CHAR(1),
  schema jsonb NOT NULL
);

CREATE TABLE queries (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL,
  source_id INT NOT NULL ,
  columns jsonb NOT NULL,
  grouped jsonb,
  aggregate jsonb NOT NULL,
  state query_state NOT NULL,
  retries INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_user
      FOREIGN KEY(user_id) 
        REFERENCES users(id)
        ON DELETE CASCADE,

  CONSTRAINT fk_source
    FOREIGN KEY(source_id)
    REFERENCES sources(id)
    ON DELETE CASCADE
);
