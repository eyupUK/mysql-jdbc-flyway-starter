package com.example.app.dao;

import com.example.app.db.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {
    public long create(String email, String first, String last) throws SQLException {
        String sql = "INSERT INTO customers(email, first_name, last_name) VALUES(?,?,?)";
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email);
            ps.setString(2, first);
            ps.setString(3, last);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public int count() throws SQLException {
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM customers");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public List<String> listEmails() throws SQLException {
        List<String> emails = new ArrayList<>();
        try (Connection con = DB.get();
             PreparedStatement ps = con.prepareStatement("SELECT email FROM customers ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) emails.add(rs.getString(1));
        }
        return emails;
    }
}
