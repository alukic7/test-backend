CREATE SCHEMA IF NOT EXISTS paymentschema AUTHORIZATION "test-user";

CREATE TABLE IF NOT EXISTS paymentschema.users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS paymentschema.accounts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES paymentschema.users(id) ON DELETE CASCADE,
    balance BIGINT NOT NULL DEFAULT 0,
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS paymentschema.transactions (
    id SERIAL PRIMARY KEY,
    from_account_id INTEGER NOT NULL REFERENCES paymentschema.accounts(id) ON DELETE RESTRICT,
    to_account_id INTEGER NOT NULL REFERENCES paymentschema.accounts(id) ON DELETE RESTRICT,
    amount BIGINT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS paymentschema.sessions (
    id UUID PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES paymentschema.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    expires_at TIMESTAMP NOT NULL,
    is_valid BOOLEAN NOT NULL DEFAULT TRUE,
    csrf_token UUID NOT NULL
    );

CREATE INDEX IF NOT EXISTS sessions_token_idx
    ON paymentschema.sessions(id)
    WHERE is_valid = TRUE;
