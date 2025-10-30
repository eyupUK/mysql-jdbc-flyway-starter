package com.example.app.db;

import com.example.app.Env;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(Env.url(), Env.user(), Env.pass());
    }
}
