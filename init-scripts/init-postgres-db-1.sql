CREATE TABLE IF NOT EXISTS postgres_db_1_users
(
    postgres1_user_id       VARCHAR(255) PRIMARY KEY,
    postgres1_user_username VARCHAR(255) NOT NULL,
    postgres1_user_name     VARCHAR(255) NOT NULL,
    postgres1_user_surname  VARCHAR(255) NOT NULL
);
INSERT INTO postgres_db_1_users (postgres1_user_id, postgres1_user_username, postgres1_user_name,
                                 postgres1_user_surname)
VALUES ('2', 'postgres1', 'Default', 'User1')
ON CONFLICT (postgres1_user_id) DO NOTHING;