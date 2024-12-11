CREATE TABLE IF NOT EXISTS mysql_db_2_user (
                                               MySql2id VARCHAR(255) PRIMARY KEY,
    Mysql2Username VARCHAR(255) NOT NULL,
    Mysql2Name VARCHAR(255) NOT NULL,
    Mysql2Surname VARCHAR(255) NOT NULL
    );
INSERT INTO mysql_db_2_user (MySql2id, Mysql2Username, Mysql2Name, Mysql2Surname)
VALUES ('1', 'mysql2', 'Default', 'User4')
    ON DUPLICATE KEY UPDATE MySql2id=MySql2id;