/**
 * This class is responsible for generating the Tf-Idf vectors for each website and computing the relevance of a query to each website.
 * Braden Zingler
 * 5/18/2024
 */
package com.java;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TfIdfGenerator {
    private Connection conn = null;
    private Integer totalNumUrls;
    private Map<String, Integer> keywordUrlCounts;
    private Lemmatizer lemmatizer;

    public TfIdfGenerator(Lemmatizer lem) {
        connectToDatabase();
        this.lemmatizer = lem;

        // these methods only need to run once to generate the Tf-Idf vectors
        // getTotalNumUrls(); 
        // getKeywordUrlCounts();
        // computeTfIdfVectors();
    }


    private void connectToDatabase() {
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:C:/Users/Braden Zingler/Desktop/Summer projects/web_crawler/wikipedia.db");
        } catch (SQLException e) {
            System.out.println("Failed to connect to database: " + e.getMessage());
        }
    }


    /**
     * Method to compute the term frequencies of the keywords of each website.
     */
    public void computeTfIdfVectors() {
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement updateStmt = null;

        try {
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            String query = "SELECT u.url_id, k.keyword, uk.num_occurences, u.num_keywords, uk.keyword_id " +
            "FROM urls u " +
            "JOIN url_keywords uk ON u.url_id = uk.url_id " +
            "JOIN keywords k ON uk.keyword_id = k.keyword_id";
            rs = stmt.executeQuery(query);


            String updateQuery = "UPDATE url_keywords SET tfidf_value = ? WHERE url_id = ? AND keyword_id = ?";
            updateStmt = conn.prepareStatement(updateQuery);

            // iterate over each keyword
            while(rs.next()) {
                int totalNumKeywords = rs.getInt("num_keywords");
                String keyword = rs.getString("keyword");
                int numOccurences = rs.getInt("num_occurences");
                int numUrlsWithKeyword = this.keywordUrlCounts.get(keyword);
                double tf = (double) numOccurences / totalNumKeywords;
                double idf = Math.log((double) this.totalNumUrls / numUrlsWithKeyword);
                double tfIdf = tf * idf;

                updateStmt.setDouble(1, tfIdf);
                updateStmt.setInt(2, rs.getInt("url_id"));
                updateStmt.setInt(3, rs.getInt("keyword_id"));
                updateStmt.executeUpdate();
            }

            conn.commit();
            rs.close();
            stmt.close();
            updateStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();  // Rollback the transaction on error
                }
            } catch (SQLException ex) {
                System.out.println("Error while rolling back transaction: " + ex.getMessage());
            }
        }
    }


    private Map<String, Integer> getKeywordUrlCounts() {
        this.keywordUrlCounts = new HashMap<>();
        String query = "SELECT k.keyword, COUNT(*) AS url_count " +
                "FROM keywords k " +
                "JOIN url_keywords uk ON k.keyword_id = uk.keyword_id " +
                "GROUP BY k.keyword";

        try (Statement stmt = this.conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String keyword = rs.getString("keyword");
                int urlCount = rs.getInt("url_count");
                keywordUrlCounts.put(keyword, urlCount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keywordUrlCounts;
    }


    private void getTotalNumUrls() {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = this.conn.createStatement();
            this.conn.setAutoCommit(false);
            String query = "SELECT COUNT(*) FROM urls";
            rs = stmt.executeQuery(query);
            this.totalNumUrls = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer getKeywordId(String keyword) {
        String query = "SELECT keyword_id FROM keywords WHERE keyword = ?";
        try (PreparedStatement stmt = this.conn.prepareStatement(query)) {
            stmt.setString(1, keyword);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("keyword_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public List<String> search(String query) {
        List<String> results = new ArrayList<>();
        String[] keywords = query.split("\\s+");
        keywords = this.lemmatizer.lemmatizeWords(keywords);
        Set<Integer> keywordIds = new HashSet<>();
        
        // Get keyword ids
        for (String keyword : keywords) {
            int keywordId = getKeywordId(keyword.toLowerCase());
            if (keywordId != -1) {
                keywordIds.add(keywordId);
            }
        }

        String sql = "SELECT DISTINCT u.url, SUM(uk.tfidf_value) AS relevance_score " +
                 "FROM urls u " +
                 "JOIN url_keywords uk ON u.url_id = uk.url_id " +
                 "WHERE uk.keyword_id IN (" + String.join(",", Collections.nCopies(keywordIds.size(), "?")) + ") " +
                 "GROUP BY u.url " +
                 "ORDER BY relevance_score DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        int index = 1;
        for (int keywordId : keywordIds) {
            pstmt.setInt(index++, keywordId);
        }
        
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            results.add(rs.getString("url"));
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}
