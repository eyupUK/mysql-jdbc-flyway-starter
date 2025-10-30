package com.example.app.seed;

import com.example.app.db.DB;
import com.github.javafaker.Faker;
import org.fluttercode.datafactory.impl.DataFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Random;

public class Seeder {
    public static void seed(int customers, int products, int orders) throws SQLException {
        Faker faker = new Faker(Locale.UK);
        DataFactory df = new DataFactory();
        Random rnd = new Random(1234);

        try (Connection con = DB.get()) {
            con.setAutoCommit(false);
            // customers
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO customers(email, first_name, last_name) VALUES (?,?,?)")) {
                for (int i = 0; i < customers; i++) {
                    String email = faker.internet().emailAddress();
                    String first = df.getFirstName();
                    String last = df.getLastName();
                    ps.setString(1, email);
                    ps.setString(2, first);
                    ps.setString(3, last);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            // products
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO products(sku, name, price) VALUES (?,?,?)")) {
                for (int i = 0; i < products; i++) {
                    String sku = "SKU-" + (100000 + i);
                    String name = faker.commerce().productName();
                    double price = 1 + rnd.nextInt(10_000) / 100.0;
                    ps.setString(1, sku);
                    ps.setString(2, name);
                    ps.setDouble(3, price);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // orders
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO orders(customer_id, product_id, quantity) VALUES (?,?,?)")) {
                for (int i = 0; i < orders; i++) {
                    long customerId = 1 + rnd.nextInt(customers);
                    long productId = 1 + rnd.nextInt(products);
                    int quantity = 1 + rnd.nextInt(5);
                    ps.setLong(1, customerId);
                    ps.setLong(2, productId);
                    ps.setInt(3, quantity);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            con.commit();
        }
    }
}
