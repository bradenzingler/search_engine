package com.java;

import java.util.*;

public class SearchEngine {
    private Lemmatizer lem;

    public SearchEngine() {
        this.lem = new Lemmatizer();
    }

    
    public List<List<String>> search(String query) {
        TfIdfCalculator calculator = new TfIdfCalculator();
        Query q = new Query(query, this.lem);
        List<List<String>> results = calculator.getRelevantDocuments(q.getQueryKeywords());
        calculator.db.closeConnection();
        return results;
    }
}