package com.zemnitskiy.aggregatehub;

import com.zemnitskiy.aggregatehub.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

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

    // Configure dynamic properties for the application context
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL DB1 with default mapping
        registry.add("aggregate-hub.data-sources[0].name", () -> "postgres-db-1");
        registry.add("aggregate-hub.data-sources[0].strategy", () -> "postgres");
        registry.add("aggregate-hub.data-sources[0].url", postgres1::getJdbcUrl);
        registry.add("aggregate-hub.data-sources[0].user", postgres1::getUsername);
        registry.add("aggregate-hub.data-sources[0].password", postgres1::getPassword);
        registry.add("aggregate-hub.data-sources[0].driverClassName", () -> "org.postgresql.Driver");
        registry.add("aggregate-hub.data-sources[0].table", () -> "d0User");

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

    /**
     * Cleans all databases before each test by deleting all records from each user table.
     */
    @BeforeEach
    void cleanDatabases() {
        List<String> dbNames = List.of("postgres-db-1", "postgres-db-2", "mysql-db-1", "mysql-db-2");
        for (String dbName : dbNames) {
            String tableName = getTableName(dbName);
            try (Connection conn = getConnection(dbName);
                 Statement stmt = conn.createStatement()) {
                String deleteQuery = String.format("DELETE FROM %s", tableName);
                stmt.executeUpdate(deleteQuery);
            } catch (Exception e) {
                Assertions.fail("Error cleaning database " + dbName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Retrieves the table name for the given database.
     *
     * @param dbName The name of the database.
     * @return The corresponding table name.
     */
    private String getTableName(String dbName) {
        return switch (dbName) {
            case "postgres-db-1" -> "d0User";
            case "postgres-db-2" -> "Postgres2User";
            case "mysql-db-1" -> "MySql1User";
            case "mysql-db-2" -> "testtablename";
            default -> throw new IllegalArgumentException("Unknown database name: " + dbName);
        };
    }

    @Test
    @DisplayName("Add User via Endpoint and Verify in All Databases")
    void testAddUserViaEndpoint() {
        // Create a new user
        User user = new User();
        user.setId("1");
        user.setName("John");
        user.setSurname("Doe");
        user.setUsername("johndoe");

        // Send a POST request to add the user via the endpoint
        ResponseEntity<Void> response = restTemplate.postForEntity("/users/save", user, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify that the user is added to all databases
        verifyUserInDatabase("postgres-db-1", "1", user);
        verifyUserInDatabase("postgres-db-2", "1", user);
        verifyUserInDatabase("mysql-db-1",  "1", user);
        verifyUserInDatabase("mysql-db-2",  "1", user);
    }

    @Test
    @DisplayName("Retrieve Users via REST API")
    void testRetrieveUsersViaApi() {
        // Add users directly to the databases
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

        // Retrieve all users via the endpoint
        ResponseEntity<User[]> getAllResponse = restTemplate.getForEntity("/users", User[].class);
        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] users = getAllResponse.getBody();
        assertThat(users).isNotNull().containsExactlyInAnyOrder(
                user1, user1, user1, user1,
                user2, user2, user2, user2
        );
    }

    @Test
    @DisplayName("Filtering Users via REST API")
    void testFilterUsersViaApi() {
        // Add users directly to the databases
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

        // Filter by username "carolwilliams"
        ResponseEntity<User[]> responseCarol = restTemplate.getForEntity("/users?username=carolwilliams", User[].class);
        assertThat(responseCarol.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] filteredCarol = responseCarol.getBody();
        assertThat(filteredCarol).isNotNull().containsExactly(
                user1, user1, user1, user1
        );

        // Filter by surname "Brown"
        ResponseEntity<User[]> responseBrown = restTemplate.getForEntity("/users?surname=Brown", User[].class);
        assertThat(responseBrown.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] filteredBrown = responseBrown.getBody();
        assertThat(filteredBrown).isNotNull().containsExactly(
                user2, user2, user2, user2
        );

        // Filter by username "carolwilliams"
        ResponseEntity<User[]> responseCarolUsername = restTemplate.getForEntity("/users?username=carolwilliams", User[].class);
        assertThat(responseCarolUsername.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] filteredCarolUsername = responseCarolUsername.getBody();
        assertThat(filteredCarolUsername).isNotNull().containsExactly(
                user1, user1, user1, user1
        );

        // Filter with a non-existent criterion
        ResponseEntity<User[]> responseNone = restTemplate.getForEntity("/users?name=NonExistent", User[].class);
        assertThat(responseNone.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] filteredNone = responseNone.getBody();
        assertThat(filteredNone).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Direct Database Interaction for Retrieval Operations")
    void testDirectDatabaseRetrieval() {
        // Add a user directly to all databases
        User user = new User("6", "Eve", "Davis", "evedavis");

        addUserDirectly("mysql-db-1", user);
        addUserDirectly("postgres-db-1", user);
        addUserDirectly("postgres-db-2", user);
        addUserDirectly("mysql-db-2", user);

        // Retrieve all users via the endpoint and verify the presence of the added user
        ResponseEntity<User[]> getAllResponse = restTemplate.getForEntity("/users", User[].class);
        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        User[] users = getAllResponse.getBody();
        assertThat(users).isNotNull().contains(
                user, user, user, user
        );
    }

    /**
     * Helper method to verify the presence of a user in the specified database.
     *
     * @param dbName       The name of the database.
     * @param idValue      The ID value to search for.
     * @param expectedUser The expected user object.
     */
    private void verifyUserInDatabase(String dbName, String idValue, User expectedUser) {
        try (Connection conn = getConnection(dbName)) {
            String tableName = getTableName(dbName);
            String idColumn = getMappedColumnName(dbName, "id");
            Statement stmt = conn.createStatement();
            String query = String.format("SELECT * FROM %s WHERE %s = '%s'", tableName, idColumn, idValue);
            ResultSet rs = stmt.executeQuery(query);
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString(idColumn)).isEqualTo(expectedUser.getId());
            assertThat(rs.getString(getMappedColumnName(dbName, "name"))).isEqualTo(expectedUser.getName());
            assertThat(rs.getString(getMappedColumnName(dbName, "surname"))).isEqualTo(expectedUser.getSurname());
            assertThat(rs.getString(getMappedColumnName(dbName, "username"))).isEqualTo(expectedUser.getUsername());
            rs.close();
            stmt.close();
        } catch (Exception e) {
            Assertions.fail("Error verifying user in " + dbName + ": " + e.getMessage());
        }
    }

    /**
     * Helper method to add a user directly to the specified database.
     * Now it doesn't require idValue or idColumn as parameters.
     * They are determined from the user's ID and the database mappings.
     *
     * @param dbName The name of the database.
     * @param user   The user object to insert.
     */
    private void addUserDirectly(String dbName, User user) {
        String tableName = getTableName(dbName);
        String idColumn = getMappedColumnName(dbName, "id");
        try (Connection conn = getConnection(dbName)) {
            Statement stmt = conn.createStatement();
            String insert = String.format(
                    "INSERT INTO %s (%s, %s, %s, %s) VALUES ('%s', '%s', '%s', '%s')",
                    tableName,
                    idColumn,
                    getMappedColumnName(dbName, "name"),
                    getMappedColumnName(dbName, "surname"),
                    getMappedColumnName(dbName, "username"),
                    user.getId(),
                    user.getName(),
                    user.getSurname(),
                    user.getUsername()
            );
            stmt.executeUpdate(insert);
            stmt.close();
        } catch (Exception e) {
            Assertions.fail("Error adding user directly to " + dbName + ": " + e.getMessage());
        }
    }

    /**
     * Helper method to obtain a connection to the specified database.
     *
     * @param dbName The name of the database.
     * @return A JDBC connection to the specified database.
     * @throws Exception If an error occurs while connecting.
     */
    private Connection getConnection(String dbName) throws Exception {
        return switch (dbName) {
            case "postgres-db-1" -> postgres1.createConnection("");
            case "postgres-db-2" -> postgres2.createConnection("");
            case "mysql-db-1" -> mysql1.createConnection("");
            case "mysql-db-2" -> mysql2.createConnection("");
            default -> throw new IllegalArgumentException("Unknown database name: " + dbName);
        };
    }

    /**
     * Helper method to get the mapped column name based on the logical name for the specified database.
     *
     * @param dbName      The name of the database.
     * @param logicalName The logical name of the column.
     * @return The actual column name in the database.
     */
    private String getMappedColumnName(String dbName, String logicalName) {
        Map<String, Map<String, String>> mappings = new HashMap<>();

        // PostgreSQL DB1
        mappings.put("postgres-db-1", Map.of(
                "id", "id",
                "name", "name",
                "surname", "surname",
                "username", "username"
        ));

        // PostgreSQL DB2
        mappings.put("postgres-db-2", Map.of(
                "id", "Postgres2id",
                "name", "Postgres2Name",
                "surname", "Postgres2Surname",
                "username", "Postgres2Username"
        ));

        // MySQL DB1
        mappings.put("mysql-db-1", Map.of(
                "id", "MySql1id",
                "name", "MySql1Name",
                "surname", "MySql1Surname",
                "username", "MySql1Username"
        ));

        // MySQL DB2
        mappings.put("mysql-db-2", Map.of(
                "id", "MySql2id",
                "name", "MySql2Name",
                "surname", "MySql2Surname",
                "username", "MySql2Username"
        ));

        return mappings.get(dbName).get(logicalName);
    }
}