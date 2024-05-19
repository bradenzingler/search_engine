/**
 * This class is responsible for generating the Tf-Idf vectors for each website and computing the relevance of a query to each website.
 * Braden Zingler
 * 5/18/2024
 */
package com.java;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
        getTotalNumUrls(); 
        getKeywordUrlCounts();
        // computeTfIdfVectors();
    }


    /**
     * Connect to the SQLite database.
     */
    private void connectToDatabase() {
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:C:/Users/Braden Zingler/Desktop/Summer projects/web_crawler/wikipedia.db");
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


    /**
     * Get the total number of URLs in the database.
     * @return The total number of URLs
     * @throws SQLException
     */
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
            return Math.log(this.totalNumUrls / numUrls);
        }
        return 0.0;
    }


    /**
     * Compute the norm of a vector.
     * @param vector The vector to compute the norm of
     * @return The norm of the vector
     */
    private Double norm(Map<String, Double> vector) {
        Double out = 0.0;
        for (String word : vector.keySet()) {
            out += Math.pow(vector.get(word), vector.get(word));
        }
        return Math.sqrt(out);
    }


    /**
     * Compute the cosine similarity between two vectors.
     * @param docVector The document vector
     * @param queryTfIdfVector The query vector
     * @return The cosine similarity between the two vectors
     */
    private Double computeCosineSimilarity(Map<String, Double> docVector, HashMap<String, Double> queryTfIdfVector) {
        Double numerator = 0.0;

        for (String word : queryTfIdfVector.keySet()) {
            Double docWordScore = docVector.getOrDefault(word, 0.0);
            Double queryWordScore = queryTfIdfVector.get(word);
            numerator += docWordScore * queryWordScore;
        }

        Double denominator = norm(docVector) * norm(queryTfIdfVector);
        return denominator != 0 ? (numerator/denominator) : 0.0;
    }


    /**
     * Compute the relevant URLs for a given query.
     * @param queryTfIdfVector The query vector
     * @param keywordIds The keyword ids of the query
     * @return The list of relevant URLs
     */
    public ArrayList<String> computeRelevantUrls(HashMap<String, Double> queryTfIdfVector, Set<Integer> keywordIds) {
        ArrayList<String> results = new ArrayList<>();
        Map<String, Map<String, Double>> documentVectors = new HashMap<>();
        Map<String, String> metadataMap = new HashMap<>();
    
        String fetchDocsQuery = "SELECT u.url, k.keyword, uk.tfidf_value u.description u.title " +
        "FROM urls u " +
        "JOIN url_keywords uk ON u.url_id = uk.url_id " +
        "JOIN keywords k ON uk.keyword_id = k.keyword_id";

        try (Statement stmt = this.conn.createStatement(); ResultSet rs = stmt.executeQuery(fetchDocsQuery)) {
            while (rs.next()) {
                String url = rs.getString("url");
                String description = rs.getString("description");
                String title = rs.getString("title");
                String keyword = rs.getString("keyword");
                double tfidfValue = rs.getDouble("tfidf_value");

                documentVectors.computeIfAbsent(url, k -> new HashMap<>()).put(keyword, tfidfValue);
                metadataMap.put(url, title + "\t" + description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Map<String, Double> similarityScores = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : documentVectors.entrySet()) {
            String url = entry.getKey();
            Map<String, Double> docVector = entry.getValue();
            double similarity = computeCosineSimilarity(docVector, queryTfIdfVector);
            similarityScores.put(url, similarity);
        }

        similarityScores.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .forEachOrdered(e -> results.add(e.getKey()));
        return results;
    }
}
