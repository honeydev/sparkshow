CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(10) NOT NULL UNIQUE,
  password_hash VARCHAR(255),
  email VARCHAR(255) UNIQUE
)