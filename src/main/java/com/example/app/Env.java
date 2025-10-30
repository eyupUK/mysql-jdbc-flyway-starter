package com.example.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Env {
    private static String url;
    private static String user;
    private static String pass;

    public static void loadDefaults() {
        if (System.getenv("DB_URL") != null) return; // already set
        try (InputStream in = Env.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties p = new Properties();
            if (in != null) {
                p.load(in);
                System.setProperty("DB_URL", p.getProperty("DB_URL"));
                System.setProperty("DB_USER", p.getProperty("DB_USER"));
                System.setProperty("DB_PASS", p.getProperty("DB_PASS"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    public static String url() { return firstNonNull(System.getenv("DB_URL"), System.getProperty("DB_URL")); }
    public static String user() { return firstNonNull(System.getenv("DB_USER"), System.getProperty("DB_USER")); }
    public static String pass() { return firstNonNull(System.getenv("DB_PASS"), System.getProperty("DB_PASS")); }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }
}
