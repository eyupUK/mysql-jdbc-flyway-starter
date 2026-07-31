package com.example.app.service;

import com.example.app.dao.CustomerDao;
import com.example.app.dao.CustomerRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class CustomerService {
    private final CustomerRepository customers;

    public CustomerService() {
        this(new CustomerDao());
    }

    public CustomerService(CustomerRepository customers) {
        this.customers = Objects.requireNonNull(customers, "customers");
    }

    public long register(String email, String firstName, String lastName) throws SQLException {
        String normalizedEmail = normalizeEmail(email);
        return customers.create(normalizedEmail, requireName(firstName, "first name"), requireName(lastName, "last name"));
    }

    public int count() throws SQLException {
        return customers.count();
    }

    public List<String> listEmails() throws SQLException {
        return customers.listEmails();
    }

    private static String normalizeEmail(String email) {
        String normalized = requireText(email, "email").toLowerCase(Locale.ROOT);
        int at = normalized.indexOf('@');
        if (at <= 0 || at != normalized.lastIndexOf('@') || at == normalized.length() - 1) {
            throw new IllegalArgumentException("email must contain a local part and domain");
        }
        return normalized;
    }

    private static String requireName(String value, String field) {
        return requireText(value, field);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
