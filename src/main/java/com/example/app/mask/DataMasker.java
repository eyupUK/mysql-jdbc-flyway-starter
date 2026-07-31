package com.example.app.mask;

import com.example.app.Env;
import com.example.app.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public final class DataMasker {
    private static final String MASK_CUSTOMERS_SQL = """
            UPDATE customers
            SET email = CONCAT('masked+', id, '@example.invalid'),
                first_name = 'Masked',
                last_name = CONCAT('Customer ', id)
            WHERE email <> CONCAT('masked+', id, '@example.invalid')
               OR first_name <> 'Masked'
               OR last_name <> CONCAT('Customer ', id)
            """;

    public int maskCustomers() throws SQLException {
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(MASK_CUSTOMERS_SQL)) {
            return ps.executeUpdate();
        }
    }

    public static void main(String[] args) throws Exception {
        if (!isConfirmed(args)) {
            throw new IllegalArgumentException("Refusing to mask data without --confirm");
        }

        Env.loadDefaults();
        int maskedCustomers = new DataMasker().maskCustomers();
        System.out.println("Masked customers: " + maskedCustomers);
    }

    static boolean isConfirmed(String[] args) {
        return args != null && Arrays.stream(args).anyMatch("--confirm"::equals);
    }
}
