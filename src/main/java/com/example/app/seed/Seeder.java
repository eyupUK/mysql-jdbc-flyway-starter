package com.example.app.seed;

import com.example.app.db.DB;
import com.github.javafaker.Faker;
import org.fluttercode.datafactory.impl.DataFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Seeder {
    public static void seed(int customers, int products, int orders) throws SQLException {
        validateCounts(customers, products, orders);

        Faker faker = new Faker(Locale.UK);
        DataFactory df = new DataFactory();
        Random rnd = new Random(1234);

        try (Connection con = DB.get()) {
            con.setAutoCommit(false);
            try {
                List<Long> customerIds = new CustomerSeeder().seed(con, customers, faker, df);
                List<SeededProduct> seededProducts = new ProductSeeder().seed(con, products, faker, rnd);
                new OrderSeeder().seed(con, orders, customerIds, seededProducts, rnd);
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    static void validateCounts(int customers, int products, int orders) {
        if (customers < 0 || products < 0 || orders < 0) {
            throw new IllegalArgumentException("Seed counts must not be negative");
        }
        if (orders > 0 && (customers == 0 || products == 0)) {
            throw new IllegalArgumentException("Orders require at least one customer and product");
        }
    }
}
