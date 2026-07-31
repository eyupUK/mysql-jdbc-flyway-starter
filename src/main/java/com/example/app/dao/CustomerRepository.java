package com.example.app.dao;

import java.sql.SQLException;
import java.util.List;

public interface CustomerRepository {
    long create(String email, String first, String last) throws SQLException;

    int count() throws SQLException;

    List<String> listEmails() throws SQLException;
}
