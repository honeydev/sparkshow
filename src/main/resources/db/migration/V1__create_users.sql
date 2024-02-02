CREATE TABLE users ( id SERIAL PRIMARY KEY,
  username VARCHAR(10) NOT NULL UNIQUE,
  password_hash VARCHAR(255),
  email VARCHAR(255) UNIQUE
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE users_roles (
    id SERIAL PRIMARY KEY,
    role_id INTEGER,
    user_id INTEGER
);

CREATE TABLE roles_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER,
    permission_id INTEGER
);

INSERT INTO permissions (id, name) VALUES
    (1, 'READ_ANY_QUERIES'),
    (2, 'READ_OWN_QUERIES'),
    (3, 'CREATE_ANY_QUERY'),
    (4, 'CREATE_LIMITED_QUERIES');

INSERT INTO roles (id, name) VALUES
    (1, 'ADMIN'),
    (2, 'USER');

INSERT INTO roles_permissions (role_id, permission_id) VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (2, 2),
    (2, 4);
