package com.example.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class EnvTest {
    private static final String[] DB_KEYS = {"DB_URL", "DB_USER", "DB_PASS"};

    private final Map<String, String> originalProperties = new HashMap<>();

    @BeforeEach
    void rememberProperties() {
        for (String key : DB_KEYS) {
            originalProperties.put(key, System.getProperty(key));
        }
    }

    @AfterEach
    void restoreProperties() {
        for (String key : DB_KEYS) {
            String value = originalProperties.get(key);
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        }
    }

    @Test
    void loadDefaultsDoesNotOverwriteExplicitSystemProperties() {
        System.setProperty("DB_URL", "jdbc:mysql://configured-host:3306/configured");
        System.setProperty("DB_USER", "configured-user");
        System.setProperty("DB_PASS", "configured-pass");

        Env.loadDefaults();

        Assertions.assertEquals("jdbc:mysql://configured-host:3306/configured", System.getProperty("DB_URL"));
        Assertions.assertEquals("configured-user", System.getProperty("DB_USER"));
        Assertions.assertEquals("configured-pass", System.getProperty("DB_PASS"));
    }

    @Test
    void loadDefaultsReadsApplicationPropertiesWhenNoDatabaseEnvironmentIsSet() {
        for (String key : DB_KEYS) {
            Assumptions.assumeTrue(System.getenv(key) == null, key + " must not be set for this test");
            System.clearProperty(key);
        }

        Env.loadDefaults();

        Assertions.assertEquals("jdbc:mysql://localhost:3306/shopdb?useSSL=false&allowPublicKeyRetrieval=true", Env.url());
        Assertions.assertEquals("shop", Env.user());
        Assertions.assertEquals("shop_pw", Env.pass());
    }
}
