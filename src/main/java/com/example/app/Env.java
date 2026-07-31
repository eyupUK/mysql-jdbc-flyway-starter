package com.example.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Env {
    public static void loadDefaults() {
        try (InputStream in = Env.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties p = new Properties();
            if (in != null) {
                p.load(in);
                setDefault(p, "DB_URL");
                setDefault(p, "DB_USER");
                setDefault(p, "DB_PASS");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    public static String url() { return firstNonNull(System.getenv("DB_URL"), System.getProperty("DB_URL")); }
    public static String user() { return firstNonNull(System.getenv("DB_USER"), System.getProperty("DB_USER")); }
    public static String pass() { return firstNonNull(System.getenv("DB_PASS"), System.getProperty("DB_PASS")); }

    private static void setDefault(Properties properties, String key) {
        if (System.getenv(key) == null && System.getProperty(key) == null) {
            String value = properties.getProperty(key);
            if (value != null) {
                System.setProperty(key, value);
            }
        }
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }
}
