CREATE TYPE query_state AS ENUM ('new', 'running', 'finished', 'failed');

CREATE TABLE queries (
  id SERIAL PRIMARY KEY,
  user_id INT,
  columns jsonb NOT NULL,
  grouped jsonb NOT NULL,
  aggregate jsonb NOT NULL,
  state query_state NOT NULL,
  result_path VARCHAR(255),
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user
      FOREIGN KEY(user_id) 
        REFERENCES users(id)
        ON DELETE CASCADE
);
