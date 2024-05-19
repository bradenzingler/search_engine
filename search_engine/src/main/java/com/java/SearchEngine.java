package com.java;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchEngine {
    private Lemmatizer lem;

    public SearchEngine() {
        this.lem = new Lemmatizer();
    }


     public List<String> search(String query) {
        TfIdfCalculator calculator = new TfIdfCalculator();
        Query q = new Query(query, this.lem);
        List<String> filteredQueryWords = q.cleanAndFilterQuery();
        for (String word : filteredQueryWords) {
            System.out.println(word);
        }
        HashMap<String, Double> queryTfVector = q.getTfVector(filteredQueryWords);
        HashMap<String, Double> queryTfIdfVector = new HashMap<>();
        
        // convert to tf * idf vector
        for (String term : queryTfVector.keySet()) {
            Double tf = queryTfVector.get(term);
            Double idf = calculator.getIDF(term);
            queryTfIdfVector.put(term, idf * tf);
        }
        
        Set<Integer> keywordIds = calculator.getKeywordIds(filteredQueryWords);
        List<String> results = calculator.computeRelevantUrls(queryTfIdfVector, keywordIds);

       
        return results;
    }
    
}
