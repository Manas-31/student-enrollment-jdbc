package com.college;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/college_enrollment";
    private static final String USER = "root";
    // Replace this placeholder locally. Never commit your real password to GitHub.
    private static final String PASSWORD = "password";

    private Database() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
