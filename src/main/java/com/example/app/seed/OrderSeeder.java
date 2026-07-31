package com.example.app.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

final class OrderSeeder {
    void seed(Connection connection, int count, List<Long> customerIds, List<SeededProduct> products, Random random)
            throws SQLException {
        try (PreparedStatement order = connection.prepareStatement(
                "INSERT INTO orders(customer_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement item = connection.prepareStatement(
                     "INSERT INTO order_items(order_id, product_id, quantity, unit_price) VALUES (?,?,?,?)")) {
            for (int i = 0; i < count; i++) {
                long customerId = customerIds.get(random.nextInt(customerIds.size()));
                SeededProduct product = products.get(random.nextInt(products.size()));
                int quantity = 1 + random.nextInt(5);

                order.setLong(1, customerId);
                order.executeUpdate();

                item.setLong(1, readGeneratedId(order));
                item.setLong(2, product.id());
                item.setInt(3, quantity);
                item.setBigDecimal(4, product.price());
                item.addBatch();
            }
            item.executeBatch();
        }
    }

    private static long readGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("Creating an order did not return a generated ID");
            }
            return keys.getLong(1);
        }
    }
}
