package com.example.app;

import com.example.app.db.DB;
import com.example.app.seed.Seeder;
import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        // Load env (properties) for local dev
        Env.loadDefaults();

        // Run Flyway migrations
        Flyway flyway = Flyway.configure()
                .dataSource(Env.url(), Env.user(), Env.pass())
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();

        // Optionally seed some data (default 20)
        int n = Optional.ofNullable(System.getenv("SEED_COUNT"))
                .map(Integer::parseInt).orElse(20);
        Seeder.seed(n, n * 3, n * 5);

        // Simple query demo
        try (Connection con = DB.get();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM customers")) {
            rs.next();
            System.out.println("Customers in DB: " + rs.getLong(1));
        }
    }
}
