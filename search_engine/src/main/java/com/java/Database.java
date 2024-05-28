package com.java;

import java.sql.*;


public class Database {
    private Connection conn;


    public Database() {
        connectToDatabase();
    }


    private void connectToDatabase() {
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:huge_data.db");
        } catch (SQLException e) {
            System.out.println("Failed to connect to database: " + e.getMessage());
        }
    }


    public ResultSet runQuery(String query) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.conn.createStatement();
            conn.setAutoCommit(false);
            rs = stmt.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Failed to run query: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Error while rolling back transaction: " + ex.getMessage());
            }
        }
        return rs;
    }


    public PreparedStatement prepareQuery(String query) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            System.out.println("Failed to prepare query: " + e.getMessage());
        } 
        return stmt;
    }


    public void runPreparedQuery(PreparedStatement stmt, Object... args) {
        try {
            for (int i = 0; i < args.length; i++) {
                stmt.setObject(i + 1, args[i]);
            }
            stmt.executeUpdate();
            this.conn.commit();
        } catch (SQLException e) {
            System.out.println("Failed to run prepared query: " + e.getMessage());
        }
    }


     /**
     * Close the database connection.
     */
    public void closeConnection() {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}