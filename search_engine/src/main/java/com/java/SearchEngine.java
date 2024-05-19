package com.java;

import java.util.*;

public class SearchEngine {
    private Lemmatizer lem;

    public SearchEngine() {
        long t0 = System.currentTimeMillis();
        this.lem = new Lemmatizer();
        long t1 = System.currentTimeMillis();
        System.out.println("Time to initialize Lemmatizer: " + (t1 - t0) + " ms");
    }

    public List<List<String>> search(String query) {
        long t0 = System.currentTimeMillis();
        TfIdfCalculator calculator = new TfIdfCalculator();
        long t1 = System.currentTimeMillis();
        System.out.println("Time to initialize TfIdfCalculator: " + (t1 - t0) + " ms");
        Query q = new Query(query, this.lem);
        List<String> filteredQueryWords = q.cleanAndFilterQuery();
        Set<Integer> keywordIds = calculator.getKeywordIds(filteredQueryWords);
        List<List<String>> results = calculator.computeRelevantUrls(keywordIds);
        calculator.closeConnection(); // close the database connection
        return results;
    }
}
