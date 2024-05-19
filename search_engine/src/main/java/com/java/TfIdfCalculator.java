package com.java;

import java.sql.*;
import java.util.*;

public class TfIdfCalculator {
    private Connection conn = null;
    private Integer totalNumUrls;
    private Map<String, Integer> keywordUrlCounts;

    /**
     * Constructor for the TfIdfCalculator class.
     */
    public TfIdfCalculator() {
        connectToDatabase();

        // these methods only need to run once to generate the Tf-Idf vectors
        // getTotalNumUrls(); 
        // getKeywordUrlCounts();
        // computeTfIdfVectors();
        // System.out.println("Tf-Idf vectors computed successfully.");
    }

    /**
     * Connect to the SQLite database.
     */
    private void connectToDatabase() {
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:C:/Users/Braden Zingler/Desktop/Summer projects/web_crawler/data.db");
        } catch (SQLException e) {
            System.out.println("Failed to connect to database: " + e.getMessage());
        }
    }

    /**
     * Compute the Tf-Idf vectors for each URL in the database.
     * @throws SQLException
     */
    public void computeTfIdfVectors() {
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement updateStmt = null;

        try {
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            String query = "SELECT u.url_id, k.keyword, uk.num_occurences, u.num_keywords, u.title, uk.keyword_id " +
                    "FROM urls u " +
                    "JOIN url_keywords uk ON u.url_id = uk.url_id " +
                    "JOIN keywords k ON uk.keyword_id = k.keyword_id";
            rs = stmt.executeQuery(query);

            String updateQuery = "UPDATE url_keywords SET tfidf_value = ? WHERE url_id = ? AND keyword_id = ?";
            updateStmt = conn.prepareStatement(updateQuery);

            // iterate over each keyword
            while (rs.next()) {
                int totalNumKeywords = rs.getInt("num_keywords");
                String keyword = rs.getString("keyword");
                String title = rs.getString("title");
                int numOccurrences = rs.getInt("num_occurences");
                if (title.contains(keyword)) {
                    numOccurrences += 50; // give higher priority to keywords in the title
                }
                int numUrlsWithKeyword = this.keywordUrlCounts.get(keyword);
                double tf = (double) numOccurrences / totalNumKeywords;
                double idf = Math.log((double) this.totalNumUrls / numUrlsWithKeyword);
                double tfIdf = tf * idf;

                updateStmt.setDouble(1, tfIdf);
                updateStmt.setInt(2, rs.getInt("url_id"));
                updateStmt.setInt(3, rs.getInt("keyword_id"));
                updateStmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();  // Rollback the transaction on error
                }
            } catch (SQLException ex) {
                System.out.println("Error while rolling back transaction: " + ex.getMessage());
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (updateStmt != null) updateStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the number of URLs that contain each keyword.
     * @return A map of keywords to the number of URLs that contain them
     */
    private Map<String, Integer> getKeywordUrlCounts() {
        this.keywordUrlCounts = new HashMap<>();
        String query = "SELECT k.keyword, COUNT(*) AS url_count " +
                "FROM keywords k " +
                "JOIN url_keywords uk ON k.keyword_id = uk.keyword_id " +
                "GROUP BY k.keyword";

        try (Statement stmt = this.conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
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

    /**
     * Get the total number of URLs in the database.
     * @return The total number of URLs
     */
    private void getTotalNumUrls() {
        try (Statement stmt = this.conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM urls")) {
            this.totalNumUrls = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the keyword ids for a list of keywords.
     * @param keywords The list of keywords
     * @return The set of keyword ids
     */
    public Set<Integer> getKeywordIds(List<String> keywords) {
        Set<Integer> ids = new HashSet<>();

        for (String keyword : keywords) {
            String query = "SELECT keyword_id FROM keywords WHERE keyword = ?";
            try (PreparedStatement stmt = this.conn.prepareStatement(query)) {
                stmt.setString(1, keyword);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ids.add(rs.getInt("keyword_id"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return ids;
    }

    /**
     * Get the inverse document frequency of a word.
     * @param queryWord The word to get the IDF of
     * @return The IDF of the word
     */
    public Double getIDF(String queryWord) {
        Integer numUrls = this.keywordUrlCounts.get(queryWord);
        if (numUrls != null) {
            return Math.log((double) this.totalNumUrls / numUrls);
        }
        return 0.0;
    }

    /**
     * Compute the relevant URLs for a given query.
     * @param keywordIds The keyword ids of the query
     * @return The list of relevant URLs
     */
    public List<List<String>> computeRelevantUrls(Set<Integer> keywordIds) {
        List<List<String>> results = new ArrayList<>();

        String sql = "SELECT DISTINCT u.url, u.title, u.description, SUM(uk.tfidf_value) AS relevance_score " +
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
                List<String> metadata = new ArrayList<>();
                metadata.add(rs.getString("url"));
                metadata.add(rs.getString("title"));
                results.add(metadata);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
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
