package com.java;

import java.util.*;

public class SearchEngine {
    private Lemmatizer lem;
    private List<List<String>> results;

    public SearchEngine() {
        this.lem = new Lemmatizer();
    }

    
    /**
     * Searches the database for relevant documents based on the user query.
     * @param query The user query
     * @return The list of relevant documents
     */
    public List<List<String>> search(String query) {
        TfIdfCalculator calculator = new TfIdfCalculator();
        Query q = new Query(query, this.lem);
        this.results = calculator.getRelevantDocuments(q.getQueryKeywords());
        calculator.db.closeConnection();
        return this.results;
    }
}