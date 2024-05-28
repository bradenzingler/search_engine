/**
 * DocumentCalculator.java
 * This class is responsible for querying the database and returning the relevant documents based on
 * the precomputed TF-IDF values.
 * Braden Zingler
 * 5/28/2024
 */


package com.java;
import java.sql.*;
import java.util.*;


public class DocumentCalculator {

    public Database db;         // The database object used to query the database

    
    /**
     * Constructor for the TfIdfCalculator class.
     */
    public DocumentCalculator() {
        this.db = new Database();
    }


    /**
     * Gets the keyword ID associated with a keyword.
     * @param keyword The keyword to search for
     * @return in the keyword id.
     */
    private int getKeywordId(String keyword) {
        PreparedStatement stmt = db.prepareQuery(Statements.GET_KEYWORD_ID);

        try {
            stmt.setString(1, keyword);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    
    /**
     * Calculates the relevant documents to return to the user.
     * @param queryKeywords the keywords to search for.
     * @return a list of relevant documents.
     */
    public List<List<String>> getRelevantDocuments(List<String> queryKeywords) {

        List<List<String>> relevantDocuments = new ArrayList<>();       // The list of relevant documents
        List<String> descriptions = new ArrayList<>();                  // The list of descriptions to track duplicates
        Map<String, Double> documentScores = new HashMap<>();           // The map of document scores for sorting

        // Find relevant documents for each keyword in the query
        for (String keyword : queryKeywords) {
            int keyword_id = getKeywordId(keyword);
            if (keyword_id == -1) continue;
            PreparedStatement stmt = db.prepareQuery(Statements.GET_DOCUMENTS_FOR_KEYWORD);

            try {
                stmt.setInt(1, keyword_id);
                ResultSet rs = stmt.executeQuery();

                // Limit the number to 100 documents to speed up return times
                while (rs.next() && relevantDocuments.size() < 100){
                    // Track duplicates with different id urls, skip if the same
                    String description = rs.getString("description");
                    if (description.length() > 100) {
                        description = description.substring(0, 100) + "...";
                    }
                    if (descriptions.contains(description)) continue;

                    descriptions.add(description);

                    // get rest of document information
                    Double tfidf = rs.getDouble("tfidf");
                    String url = rs.getString("url");
                    String title = rs.getString("title");
                    
                    // store resulting documents
                    List<String> document = Arrays.asList(url, title, description);
                    documentScores.put(url, documentScores.getOrDefault(url, 0.0) + tfidf);
                    relevantDocuments.add(document);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Sort the documents by their tfidf scores
        relevantDocuments.sort((a, b) -> {
            String urlA = a.get(0);
            String urlB = b.get(0);
            return Double.compare(documentScores.get(urlB), documentScores.get(urlA));
        });

        return relevantDocuments;
    }
}