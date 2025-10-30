package com.example.app;

import com.example.app.dao.CustomerDao;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryTests {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("shopdb")
            .withUsername("shop")
            .withPassword("shop_pw");

    @BeforeAll
    static void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword())
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        // configure system props for DAO code to pick up
        System.setProperty("DB_URL", mysql.getJdbcUrl());
        System.setProperty("DB_USER", mysql.getUsername());
        System.setProperty("DB_PASS", mysql.getPassword());
    }

    @Test @Order(1)
    void schemaCreated() throws Exception {
        try (Connection con = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SHOW TABLES")) {
            int count = 0;
            while (rs.next()) count++;
            Assertions.assertTrue(count >= 4, "Expected at least 4 tables");
        }
    }

    @Test @Order(2)
    void crudWithJdbc() throws Exception {
        CustomerDao dao = new CustomerDao();
        long id = dao.create("alice@example.com", "Alice", "Liddell");
        Assertions.assertTrue(id > 0);
        Assertions.assertEquals(1, dao.count());
    }
}
