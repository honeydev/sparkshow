CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(10) NOT NULL UNIQUE,
  password_hash VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
   created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
   updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE users_roles (
    id SERIAL PRIMARY KEY,
    role_id INTEGER,
    user_id INTEGER,

    CONSTRAINT fk_user
      FOREIGN KEY(user_id) 
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_roles
      FOREIGN KEY(role_id) 
        REFERENCES roles(id)
        ON DELETE CASCADE
);

CREATE TABLE roles_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER,
    permission_id INTEGER,
    CONSTRAINT fk_role
      FOREIGN KEY(role_id) 
        REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_permissions
      FOREIGN KEY(permission_id) 
        REFERENCES permissions(id)
        ON DELETE CASCADE

);

INSERT INTO permissions (id, name) VALUES
    (1, 'READ_ANY_QUERIES'),
    (2, 'READ_OWN_QUERIES'),
    (3, 'CREATE_ANY_QUERY'),
    (4, 'CREATE_LIMITED_QUERIES');

INSERT INTO roles (id, name) VALUES
    (1, 'ADMIN'),
    (2, 'USER'),
    (3, 'MANAGER');

INSERT INTO roles_permissions (role_id, permission_id) VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (2, 2),
    (2, 4);
