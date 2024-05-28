/**
 * SearchEngine.java
 * The main class for the search engine. 
 * This class is responsible for handling the user query and returning the relevant documents.
 * Braden Zingler
 * 5/28/2024
 */
package com.java;
import java.util.*;


public class SearchEngine {

    private Lemmatizer lem;                     // The lemmatizer object used to lemmatize the query
    private List<List<String>> results;         // The list of relevant documents and their fields


    /**
     * Constructor for the SearchEngine class.
     */
    public SearchEngine() {
        this.lem = new Lemmatizer();
    }

    
    /**
     * Searches the database for relevant documents based on the user query.
     * @param query The user query
     * @return The list of relevant documents
     */
    public List<List<String>> search(String query) {
        DocumentCalculator calculator = new DocumentCalculator();
        Query q = new Query(query, this.lem);
        this.results = calculator.getRelevantDocuments(q.getQueryKeywords());
        calculator.db.closeConnection();
        return this.results;
    }
}