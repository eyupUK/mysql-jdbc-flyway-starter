package com.example.app;

import com.example.app.seed.Seeder;
import com.example.app.service.CustomerService;
import org.flywaydb.core.Flyway;

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

        CustomerService customers = new CustomerService();
        System.out.println("Customers in DB: " + customers.count());
    }
}
