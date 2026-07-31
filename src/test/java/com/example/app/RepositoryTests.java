package com.example.app;

import com.example.app.mask.DataMasker;
import com.example.app.seed.Seeder;
import com.example.app.service.CustomerService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

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

        try (Connection con = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement st = con.createStatement();
             ResultSet columns = st.executeQuery("""
                     SELECT COUNT(*)
                     FROM information_schema.columns
                     WHERE table_schema = DATABASE()
                       AND ((table_name = 'customers' AND column_name = 'status')
                         OR (table_name = 'products' AND column_name = 'stock_quantity'))
                     """)) {
            columns.next();
            Assertions.assertEquals(2, columns.getInt(1));
        }
    }

    @Test @Order(2)
    void maskerReturnsZeroWhenThereAreNoCustomers() throws Exception {
        Assertions.assertEquals(0, new DataMasker().maskCustomers());
    }

    @Test @Order(3)
    void crudWithJdbc() throws Exception {
        CustomerService customers = new CustomerService();
        long id = customers.register(" Alice@Example.com ", " Alice ", " Liddell ");
        Assertions.assertTrue(id > 0);
        Assertions.assertEquals(1, customers.count());
        Assertions.assertEquals(List.of("alice@example.com"), customers.listEmails());
    }

    @Test @Order(4)
    void seederCreatesOrderItems() throws Exception {
        Seeder.seed(2, 3, 4);

        try (Connection con = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement st = con.createStatement();
             ResultSet orders = st.executeQuery("SELECT COUNT(*) FROM orders")) {
            orders.next();
            Assertions.assertEquals(4, orders.getInt(1));
        }

        try (Connection con = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement st = con.createStatement();
             ResultSet items = st.executeQuery("SELECT COUNT(*) FROM order_items")) {
            items.next();
            Assertions.assertEquals(4, items.getInt(1));
        }

        try (Connection con = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement st = con.createStatement();
             ResultSet inventory = st.executeQuery("SELECT COUNT(*) FROM products WHERE stock_quantity BETWEEN 10 AND 500")) {
            inventory.next();
            Assertions.assertEquals(3, inventory.getInt(1));
        }

        try (Connection con = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement st = con.createStatement();
             ResultSet seededOrderCustomers = st.executeQuery("""
                     SELECT COUNT(*)
                     FROM orders o
                     JOIN customers c ON c.id = o.customer_id
                     WHERE c.email LIKE 'seed-%@example.test'
                     """)) {
            seededOrderCustomers.next();
            Assertions.assertEquals(4, seededOrderCustomers.getInt(1));
        }
    }

    @Test @Order(5)
    void maskerReplacesCustomerPiiAndIsIdempotent() throws Exception {
        DataMasker masker = new DataMasker();
        try (Connection con = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement st = con.createStatement()) {
            st.executeUpdate("UPDATE customers SET email = CONCAT('masked+', id, '@example.invalid') WHERE id = 1");
        }

        Assertions.assertEquals(3, masker.maskCustomers());
        Assertions.assertEquals(0, masker.maskCustomers());

        try (Connection con = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement st = con.createStatement();
             ResultSet customers = st.executeQuery("SELECT id, email, first_name, last_name FROM customers ORDER BY id")) {
            int customerCount = 0;
            while (customers.next()) {
                long id = customers.getLong("id");
                Assertions.assertEquals("masked+" + id + "@example.invalid", customers.getString("email"));
                Assertions.assertEquals("Masked", customers.getString("first_name"));
                Assertions.assertEquals("Customer " + id, customers.getString("last_name"));
                customerCount++;
            }
            Assertions.assertEquals(3, customerCount);
        }

        try (Connection con = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             Statement st = con.createStatement();
             ResultSet relationships = st.executeQuery("""
                     SELECT COUNT(*)
                     FROM order_items oi
                     JOIN orders o ON o.id = oi.order_id
                     JOIN customers c ON c.id = o.customer_id
                     """)) {
            relationships.next();
            Assertions.assertEquals(4, relationships.getInt(1));
        }
    }
}
