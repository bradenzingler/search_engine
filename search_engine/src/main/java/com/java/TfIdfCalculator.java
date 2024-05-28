package com.java;

import java.sql.*;
import java.util.*;


public class TfIdfCalculator {
    public Database db;

    
    /**
     * Constructor for the TfIdfCalculator class.
     */
    public TfIdfCalculator() {
        this.db = new Database();
    }


    /**
     * Gets the keyword ID associated with a keyword.
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
        List<List<String>> relevantDocuments = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        Map<String, Double> documentScores = new HashMap<>();

        for (String keyword : queryKeywords) {
            int keyword_id = getKeywordId(keyword);
            if (keyword_id == -1) continue;
            PreparedStatement stmt = db.prepareQuery(Statements.GET_DOCUMENTS_FOR_KEYWORD);

            try {
                stmt.setInt(1, keyword_id);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
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
        long t0 = System.currentTimeMillis();
        relevantDocuments.sort((a, b) -> {
            String urlA = a.get(0);
            String urlB = b.get(0);
            return Double.compare(documentScores.get(urlB), documentScores.get(urlA));
        });
        long t1 = System.currentTimeMillis();
        System.out.println("Sorting took " + (t1 - t0) + " ms");

        return relevantDocuments;
    }
}