package com.zemnitskiy.aggregatehub;

import com.zemnitskiy.aggregatehub.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MultiDatabaseIntegrationTest {

    // PostgreSQL Containers
    @Container
    private static final PostgreSQLContainer<?> postgres1 = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("db1")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    private static final PostgreSQLContainer<?> postgres2 = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("db2")
            .withUsername("testuser")
            .withPassword("testpass");

    // MySQL Containers
    @Container
    private static final MySQLContainer<?> mysql1 = new MySQLContainer<>("mysql:8.0.32")
            .withDatabaseName("db1")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    private static final MySQLContainer<?> mysql2 = new MySQLContainer<>("mysql:8.0.32")
            .withDatabaseName("db2")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private TestRestTemplate restTemplate;

    private static class TableSchema {
        private final String strategy;
        private final String tableName;
        private final List<String> columns;

        public TableSchema(String strategy, String tableName, List<String> columns) {
            this.strategy = strategy;
            this.tableName = tableName;
            this.columns = columns;
        }

        public String getStrategy() {
            return strategy;
        }

        public String getTableName() {
            return tableName;
        }

        public List<String> getColumns() {
            return columns;
        }
    }

    private static final Map<String, TableSchema> expectedSchemas = Map.of(
            "postgres-db-1", new TableSchema(
                    "postgres",
                    "users",
                    List.of("id", "name", "surname", "username")
            ),
            "postgres-db-2", new TableSchema(
                    "postgres",
                    "postgres2user",
                    List.of("postgres2id", "postgres2name", "postgres2surname", "postgres2username")
            ),
            "mysql-db-1", new TableSchema(
                    "mysql",
                    "MySql1User",
                    List.of("MySql1id", "MySql1Name", "MySql1Surname", "MySql1Username")
            ),
            "mysql-db-2", new TableSchema(
                    "mysql",
                    "testtablename",
                    List.of("MySql2id", "MySql2Name", "MySql2Surname", "MySql2Username")
            )
    );

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL DB1 with default mapping
        registry.add("aggregate-hub.data-sources[0].name", () -> "postgres-db-1");
        registry.add("aggregate-hub.data-sources[0].strategy", () -> "postgres");
        registry.add("aggregate-hub.data-sources[0].url", postgres1::getJdbcUrl);
        registry.add("aggregate-hub.data-sources[0].user", postgres1::getUsername);
        registry.add("aggregate-hub.data-sources[0].password", postgres1::getPassword);
        registry.add("aggregate-hub.data-sources[0].driverClassName", () -> "org.postgresql.Driver");

        // PostgreSQL DB2
        registry.add("aggregate-hub.data-sources[1].name", () -> "postgres-db-2");
        registry.add("aggregate-hub.data-sources[1].strategy", () -> "postgres");
        registry.add("aggregate-hub.data-sources[1].url", postgres2::getJdbcUrl);
        registry.add("aggregate-hub.data-sources[1].user", postgres2::getUsername);
        registry.add("aggregate-hub.data-sources[1].password", postgres2::getPassword);
        registry.add("aggregate-hub.data-sources[1].driverClassName", () -> "org.postgresql.Driver");
        registry.add("aggregate-hub.data-sources[1].table", () -> "Postgres2User");
        registry.add("aggregate-hub.data-sources[1].mapping.id", () -> "Postgres2id");
        registry.add("aggregate-hub.data-sources[1].mapping.username", () -> "Postgres2Username");
        registry.add("aggregate-hub.data-sources[1].mapping.name", () -> "Postgres2Name");
        registry.add("aggregate-hub.data-sources[1].mapping.surname", () -> "Postgres2Surname");

        // MySQL DB1
        registry.add("aggregate-hub.data-sources[2].name", () -> "mysql-db-1");
        registry.add("aggregate-hub.data-sources[2].strategy", () -> "mysql");
        registry.add("aggregate-hub.data-sources[2].url", mysql1::getJdbcUrl);
        registry.add("aggregate-hub.data-sources[2].user", mysql1::getUsername);
        registry.add("aggregate-hub.data-sources[2].password", mysql1::getPassword);
        registry.add("aggregate-hub.data-sources[2].driverClassName", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("aggregate-hub.data-sources[2].table", () -> "MySql1User");
        registry.add("aggregate-hub.data-sources[2].mapping.id", () -> "MySql1id");
        registry.add("aggregate-hub.data-sources[2].mapping.username", () -> "MySql1Username");
        registry.add("aggregate-hub.data-sources[2].mapping.name", () -> "MySql1Name");
        registry.add("aggregate-hub.data-sources[2].mapping.surname", () -> "MySql1Surname");

        // MySQL DB2
        registry.add("aggregate-hub.data-sources[3].name", () -> "mysql-db-2");
        registry.add("aggregate-hub.data-sources[3].strategy", () -> "mysql");
        registry.add("aggregate-hub.data-sources[3].url", mysql2::getJdbcUrl);
        registry.add("aggregate-hub.data-sources[3].user", mysql2::getUsername);
        registry.add("aggregate-hub.data-sources[3].password", mysql2::getPassword);
        registry.add("aggregate-hub.data-sources[3].driverClassName", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("aggregate-hub.data-sources[3].table", () -> "testtablename");
        registry.add("aggregate-hub.data-sources[3].mapping.id", () -> "MySql2id");
        registry.add("aggregate-hub.data-sources[3].mapping.username", () -> "MySql2Username");
        registry.add("aggregate-hub.data-sources[3].mapping.name", () -> "MySql2Name");
        registry.add("aggregate-hub.data-sources[3].mapping.surname", () -> "MySql2Surname");
    }

    @BeforeEach
    void cleanDatabases() {
        List<String> dbNames = List.of("postgres-db-1", "postgres-db-2", "mysql-db-1", "mysql-db-2");
        for (String dbName : dbNames) {
            TableSchema schema = expectedSchemas.get(dbName);
            String tableName = schema.getTableName();
            String mappedTableName = getMappedTableNameForTest(dbName, tableName);
            try (Connection conn = getConnection(dbName);
                 Statement stmt = conn.createStatement()) {
                String deleteQuery = String.format("DELETE FROM %s", mappedTableName);
                stmt.executeUpdate(deleteQuery);
            } catch (Exception e) {
                fail("Error cleaning database " + dbName + ": " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Application should not start with incorrect database configuration")
    void testApplicationDoesNotStartWithBadConfig() {
        Map<String, Object> badProperties = new HashMap<>();
        badProperties.put("aggregate-hub.data-sources[0].url", "jdbc:postgresql://wronghost:wrongport/wrongdb");
        badProperties.put("aggregate-hub.data-sources[0].user", "wronguser");
        badProperties.put("aggregate-hub.data-sources[0].password", "wrongpass");

        // Attempt to start the application with bad configuration
        SpringApplication app = new SpringApplication(MultiDatabaseIntegrationTest.class);
        app.setDefaultProperties(badProperties);

        Throwable exception = assertThrows(Throwable.class, () -> {
            try (ConfigurableApplicationContext context = app.run()) {
                fail("Application should not have started with incorrect configuration");
            }
        });

        // Check if the exception message contains any indication of connection or configuration error
        String errorMessage = exception.getMessage();
        assertThat(errorMessage).containsAnyOf("Unable to start web server");
    }

    @Test
    @DisplayName("Verify Table Names and Column Names in Each Database")
    void testTablesAndColumns() {
        for (Map.Entry<String, TableSchema> entry : expectedSchemas.entrySet()) {
            String dbName = entry.getKey();
            TableSchema expectedSchema = entry.getValue();

            try (Connection conn = getConnection(dbName)) {
                DatabaseMetaData metaData = conn.getMetaData();

                String schemaPattern = null;
                String tableName = expectedSchema.getTableName();
                if ("postgres".equalsIgnoreCase(expectedSchema.getStrategy())) {
                    schemaPattern = "public"; // Default schema in PostgreSQL
                    tableName = tableName.toLowerCase();
                }
                String[] types = {"TABLE"};

                boolean tableExists;
                try (ResultSet tables = metaData.getTables(conn.getCatalog(), schemaPattern, tableName, types)) {
                    tableExists = tables.next();
                    assertTrue(tableExists,
                            String.format("Table '%s' does not exist in database '%s'", expectedSchema.getTableName(), dbName));
                }

                List<String> actualColumns = new ArrayList<>();
                try (ResultSet columns = metaData.getColumns(conn.getCatalog(), schemaPattern, tableName, null)) {
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        actualColumns.add(columnName);
                    }
                }

                // Verify that all expected columns are present
                List<String> missingColumns = expectedSchema.getColumns().stream()
                        .filter(expectedCol -> !actualColumns.contains(expectedCol))
                        .toList();

                assertTrue(missingColumns.isEmpty(),
                        String.format("Missing columns in table '%s' of database '%s': %s",
                                expectedSchema.getTableName(),
                                dbName,
                                missingColumns));

                // Verify that no extra columns exist
                List<String> extraColumns = actualColumns.stream()
                        .filter(actualCol -> !expectedSchema.getColumns().contains(actualCol))
                        .toList();

                assertTrue(extraColumns.isEmpty(),
                        String.format("Unexpected columns in table '%s' of database '%s': %s",
                                expectedSchema.getTableName(),
                                dbName,
                                extraColumns));

            } catch (Exception e) {
                fail("Error verifying schema for database " + dbName + ": " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Retrieve Users via REST API")
    void testRetrieveUsersViaApi() {
        User user1 = new User("2", "Alice", "Smith", "alicesmith");
        User user2 = new User("3", "Bob", "Johnson", "bobjohnson");

        addUserDirectly("postgres-db-1", user1);
        addUserDirectly("postgres-db-1", user2);
        addUserDirectly("postgres-db-2", user1);
        addUserDirectly("postgres-db-2", user2);
        addUserDirectly("mysql-db-1", user1);
        addUserDirectly("mysql-db-1", user2);
        addUserDirectly("mysql-db-2", user1);
        addUserDirectly("mysql-db-2", user2);

        ResponseEntity<User[]> getAllResponse = restTemplate.getForEntity("/users", User[].class);
        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] users = getAllResponse.getBody();
        assertThat(users).isNotNull().hasSize(8);

        List<User> userList = Arrays.asList(users);
        assertThat(userList).filteredOn(u -> u.getId().equals("2")).hasSize(4);
        assertThat(userList).filteredOn(u -> u.getId().equals("3")).hasSize(4);
    }

    @Test
    @DisplayName("Filtering Users via REST API")
    void testFilterUsersViaApi() {
        User user1 = new User("4", "carolwilliams", "Carol", "Williams");
        User user2 = new User("5", "davidbrown", "David", "Brown");

        addUserDirectly("postgres-db-1", user1);
        addUserDirectly("postgres-db-1", user2);
        addUserDirectly("postgres-db-2", user1);
        addUserDirectly("postgres-db-2", user2);
        addUserDirectly("mysql-db-1", user1);
        addUserDirectly("mysql-db-1", user2);
        addUserDirectly("mysql-db-2", user1);
        addUserDirectly("mysql-db-2", user2);

        // Filter by id "4"
        ResponseEntity<User[]> responseById = restTemplate.getForEntity("/users?id=4", User[].class);
        assertThat(responseById.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] filteredById = responseById.getBody();
        assertThat(filteredById).isNotNull().hasSize(4);
        assertThat(Arrays.stream(filteredById).allMatch(u -> u.getId().equals("4"))).isTrue();

        // Filter by username "carolwilliams"
        ResponseEntity<User[]> responseCarol = restTemplate.getForEntity("/users?username=carolwilliams", User[].class);
        assertThat(responseCarol.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] filteredCarol = responseCarol.getBody();
        assertThat(filteredCarol).isNotNull().hasSize(4);
        assertThat(Arrays.stream(filteredCarol).allMatch(u -> u.getUsername().equals("carolwilliams"))).isTrue();

        // Filter by surname "Brown"
        ResponseEntity<User[]> responseBrown = restTemplate.getForEntity("/users?surname=Brown", User[].class);
        assertThat(responseBrown.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] filteredBrown = responseBrown.getBody();
        assertThat(filteredBrown).isNotNull().hasSize(4);
        assertThat(Arrays.stream(filteredBrown).allMatch(u -> u.getSurname().equals("Brown"))).isTrue();

        // Filter by name "David"
        ResponseEntity<User[]> responseByName = restTemplate.getForEntity("/users?name=David", User[].class);
        assertThat(responseByName.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] filteredByName = responseByName.getBody();
        assertThat(filteredByName).isNotNull().hasSize(4);
        assertThat(Arrays.stream(filteredByName).allMatch(u -> u.getName().equals("David"))).isTrue();

        // Filter with a non-existent criterion
        ResponseEntity<User[]> responseNone = restTemplate.getForEntity("/users?name=NonExistent", User[].class);
        assertThat(responseNone.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] filteredNone = responseNone.getBody();
        assertThat(filteredNone).isEmpty();
    }

    private String getMappedTableNameForTest(String dbName, String tableName) {
        TableSchema schema = expectedSchemas.get(dbName);
        if (schema == null) {
            throw new IllegalArgumentException("No schema defined for database: " + dbName);
        }

        if ("postgres".equalsIgnoreCase(schema.getStrategy())) {
            return tableName.toLowerCase(); // PostgreSQL folds to lowercase
        } else {
            return tableName;
        }
    }

    private void addUserDirectly(String dbName, User user) {
        TableSchema schema = expectedSchemas.get(dbName);
        String tableName = schema.getTableName();
        String mappedTableName = getMappedTableNameForTest(dbName, tableName);

        String idColumn = getMappedColumnName(dbName, "id");
        String nameColumn = getMappedColumnName(dbName, "name");
        String surnameColumn = getMappedColumnName(dbName, "surname");
        String usernameColumn = getMappedColumnName(dbName, "username");

        try (Connection conn = getConnection(dbName);
             Statement stmt = conn.createStatement()) {
            String insert = String.format(
                    "INSERT INTO %s (%s, %s, %s, %s) VALUES ('%s', '%s', '%s', '%s')",
                    mappedTableName,
                    idColumn,
                    nameColumn,
                    surnameColumn,
                    usernameColumn,
                    user.getId(),
                    user.getName(),
                    user.getSurname(),
                    user.getUsername()
            );
            stmt.executeUpdate(insert);
        } catch (Exception e) {
            fail("Error adding user directly to " + dbName + ": " + e.getMessage());
        }
    }

    private Connection getConnection(String dbName) throws Exception {
        return switch (dbName) {
            case "postgres-db-1" -> postgres1.createConnection("");
            case "postgres-db-2" -> postgres2.createConnection("");
            case "mysql-db-1" -> mysql1.createConnection("");
            case "mysql-db-2" -> mysql2.createConnection("");
            default -> throw new IllegalArgumentException("Unknown database name: " + dbName);
        };
    }

    private String getMappedColumnName(String dbName, String logicalName) {
        Map<String, Map<String, String>> mappings = Map.of(
                "postgres-db-1", Map.of(
                        "id", "id",
                        "name", "name",
                        "surname", "surname",
                        "username", "username"
                ),
                "postgres-db-2", Map.of(
                        "id", "postgres2id",
                        "name", "postgres2name",
                        "surname", "postgres2surname",
                        "username", "postgres2username"
                ),
                "mysql-db-1", Map.of(
                        "id", "MySql1id",
                        "name", "MySql1Name",
                        "surname", "MySql1Surname",
                        "username", "MySql1Username"
                ),
                "mysql-db-2", Map.of(
                        "id", "MySql2id",
                        "name", "MySql2Name",
                        "surname", "MySql2Surname",
                        "username", "MySql2Username"
                )
        );
        Map<String, String> dbMapping = mappings.get(dbName);
        if (dbMapping == null) {
            throw new IllegalArgumentException("No column mappings defined for database: " + dbName);
        }
        String mappedName = dbMapping.get(logicalName);
        if (mappedName == null) {
            throw new IllegalArgumentException("No mapping found for column '" + logicalName + "' in database: " + dbName);
        }
        return mappedName;
    }
}