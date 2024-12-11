CREATE TABLE IF NOT EXISTS users (
                                     id VARCHAR(255) PRIMARY KEY,
                                     username VARCHAR(255) NOT NULL,
                                     name VARCHAR(255) NOT NULL,
                                     surname VARCHAR(255) NOT NULL
);

INSERT INTO users (id, username, name, surname)
VALUES ('0', 'mysql1', 'Default', 'User3')
ON DUPLICATE KEY UPDATE id=id;