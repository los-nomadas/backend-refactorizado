ALTER TABLE internal_credentials ADD COLUMN user_id BIGINT NULL;

ALTER TABLE internal_credentials ADD CONSTRAINT fk_internal_credentials_user
    FOREIGN KEY (user_id) REFERENCES users (id);
