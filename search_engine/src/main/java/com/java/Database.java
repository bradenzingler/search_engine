package com.java;

import java.sql.*;

public class Database {
    private Connection conn;

    public Database() {
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:C:/Users/Braden Zingler/Desktop/Summer projects/web_crawler/data.db");
        } catch (SQLException e) {
            System.out.println("Failed to connect to database: " + e.getMessage());
        }
    }
    
}
