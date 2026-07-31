package com.example.app.seed;

import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

final class ProductSeeder {
    List<SeededProduct> seed(Connection connection, int count, Faker faker, Random random) throws SQLException {
        List<SeededProduct> products = new ArrayList<>(count);
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO products(sku, name, price, stock_quantity) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < count; i++) {
                BigDecimal price = BigDecimal.valueOf(100 + random.nextInt(10_000), 2);
                statement.setString(1, "SKU-" + UUID.randomUUID());
                statement.setString(2, faker.commerce().productName());
                statement.setBigDecimal(3, price);
                statement.setInt(4, 10 + random.nextInt(491));
                statement.executeUpdate();
                products.add(new SeededProduct(readGeneratedId(statement), price));
            }
        }
        return products;
    }

    private static long readGeneratedId(PreparedStatement statement) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("Creating a product did not return a generated ID");
            }
            return keys.getLong(1);
        }
    }
}
