package com.example.app.seed;

import com.github.javafaker.Faker;
import org.fluttercode.datafactory.impl.DataFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class CustomerSeeder {
    List<Long> seed(Connection connection, int count, Faker faker, DataFactory dataFactory) throws SQLException {
        List<Long> customerIds = new ArrayList<>(count);
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO customers(email, first_name, last_name) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < count; i++) {
                statement.setString(1, "seed-" + UUID.randomUUID() + "@example.test");
                statement.setString(2, dataFactory.getFirstName());
                statement.setString(3, faker.name().lastName());
                statement.executeUpdate();
                customerIds.add(readGeneratedId(statement, "customer"));
            }
        }
        return customerIds;
    }

    private static long readGeneratedId(PreparedStatement statement, String recordType) throws SQLException {
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("Creating a " + recordType + " did not return a generated ID");
            }
            return keys.getLong(1);
        }
    }
}
