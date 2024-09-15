CREATE TYPE query_state AS ENUM ('NEW', 'RUNNED', 'FINISHED', 'FAILED');

CREATE TABLE queries (
  id SERIAL PRIMARY KEY,
  user_id INT,
  query TEXT NOT NULL,
  state query_state NOT NULL,
  result_path VARCHAR(255),
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user
      FOREIGN KEY(user_id) 
        REFERENCES users(id)
        ON DELETE CASCADE
);

