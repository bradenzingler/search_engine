package com.java;

import java.sql.*;
import java.util.*;


public class TfIdfCalculator {
    public Database db;
    private Integer totalNumUrls;


    /**
     * Constructor for the TfIdfCalculator class.
     */
    public TfIdfCalculator() {
        this.db = new Database();
        getTotalNumUrls();
    }


    /**
     * Get the number of URLs that contain a keyword.
     * @return The number of URLs for a keyword
     */
    private Integer getNumberDocuments(String query) {
        PreparedStatement stmt = db.prepareQuery(Statements.GET_KEYWORD_URL_COUNTS);
        try {
            stmt.setString(1, query);
            ResultSet rs = stmt.executeQuery();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * Get the total number of URLs in the database.
     * @return The total number of URLs
     */
    private void getTotalNumUrls() {
        ResultSet rs = db.runQuery("SELECT COUNT(*) FROM url_keywords");
        try {
            this.totalNumUrls = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<List<String>> getRelevantDocuments(List<String> queryKeywords) {
        List<List<String>> relevantDocuments = new ArrayList<>();

        for (String keyword : queryKeywords) {
            PreparedStatement stmt = db.prepareQuery(Statements.GET_DOCUMENTS_FOR_KEYWORD);
            try {
                stmt.setString(1, keyword);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Double tfidf = rs.getDouble(1);
                    String url = rs.getString(2);
                    String title = rs.getString(3);
                    String description = rs.getString(4);
                    List<String> document = Arrays.asList(url, title, description);
                    relevantDocuments.add(document);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return relevantDocuments;
    }
}