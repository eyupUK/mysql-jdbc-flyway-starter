package com.example.app.seed;

import com.example.app.db.DB;
import com.github.javafaker.Faker;
import org.fluttercode.datafactory.impl.DataFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
            BigDecimal[] productPrices = new BigDecimal[products];
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO products(sku, name, price) VALUES (?,?,?)")) {
                for (int i = 0; i < products; i++) {
                    String sku = "SKU-" + (100000 + i);
                    String name = faker.commerce().productName();
                    BigDecimal price = BigDecimal.valueOf(100 + rnd.nextInt(10_000), 2);
                    productPrices[i] = price;
                    ps.setString(1, sku);
                    ps.setString(2, name);
                    ps.setBigDecimal(3, price);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement order = con.prepareStatement(
                    "INSERT INTO orders(customer_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement item = con.prepareStatement(
                         "INSERT INTO order_items(order_id, product_id, quantity, unit_price) VALUES (?,?,?,?)")) {
                for (int i = 0; i < orders; i++) {
                    long customerId = 1 + rnd.nextInt(customers);
                    int productIndex = rnd.nextInt(products);
                    long productId = 1L + productIndex;
                    int quantity = 1 + rnd.nextInt(5);
                    order.setLong(1, customerId);
                    order.executeUpdate();

                    try (var keys = order.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Creating an order did not return a generated ID");
                        }
                        item.setLong(1, keys.getLong(1));
                    }
                    item.setLong(2, productId);
                    item.setInt(3, quantity);
                    item.setBigDecimal(4, productPrices[productIndex]);
                    item.addBatch();
                }
                item.executeBatch();
            }
            con.commit();
        }
    }
}
