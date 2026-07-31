package com.example.app.seed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SeederTest {
    @Test
    void acceptsZeroOrdersWithoutCustomersOrProducts() {
        Assertions.assertDoesNotThrow(() -> Seeder.validateCounts(0, 0, 0));
        Assertions.assertDoesNotThrow(() -> Seeder.validateCounts(1, 0, 0));
        Assertions.assertDoesNotThrow(() -> Seeder.validateCounts(0, 1, 0));
    }

    @Test
    void rejectsNegativeCounts() {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Seeder.validateCounts(-1, 1, 1)
        );
        Assertions.assertEquals("Seed counts must not be negative", exception.getMessage());
    }

    @Test
    void rejectsOrdersWithoutRequiredRecordsBeforeOpeningDatabaseConnection() {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Seeder.seed(0, 1, 1)
        );
        Assertions.assertEquals("Orders require at least one customer and product", exception.getMessage());
    }
}
