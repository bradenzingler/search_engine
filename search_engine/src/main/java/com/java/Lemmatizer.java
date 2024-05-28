/**
 * A class that lemmatizes keywords using a pre-defined list of lemmatizations.
 * The lemmatizations are read from a SQLite database and stored in a HashMap.
 * The lemmatizer is used in the web scraping process to ensure that the keywords are in their base form.
 * Braden Zingler
 * 5/18/2024
 */
package com.java;

import java.util.HashMap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class Lemmatizer {
    
    public HashMap<String, String> lemmas;
    private Connection conn;


    /**
     * Constructor for the Lemmatizer class.
     * Initializes the lemmas HashMap and reads the lemmatization list from a CSV file.
     */
    public Lemmatizer() {
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:lemmas.db");
        } catch (Exception e) { 
            System.out.println("An exception occurred: " + e);
        }
    }


    /**
     * Lemmatizes a keyword.
     * @param word the word to be lemmatized
     * @return the lemmatized version of the word
     */
    public String lemmatizeWord(String word) {
        String selectWord = "SELECT val FROM lemmas WHERE key = ?";
        try {
            PreparedStatement pstm = conn.prepareStatement(selectWord);
            pstm.setString(1, word);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getString("val");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return word;
    }
}
