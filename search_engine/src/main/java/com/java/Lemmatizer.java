/**
 * A class that lemmatizes keywords using a pre-defined list of lemmatizations.
 * The lemmatizations are read from a CSV file and stored in a HashMap for constant time lookups.
 * The lemmatizer is used in the web scraping process to ensure that the keywords are in their base form.
 * Braden Zingler
 * 5/18/1014
 */
package com.java;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Lemmatizer {
    public HashMap<String, String> lemmas;
    private Connection conn;


    /**
     * Constructor for the Lemmatizer class.
     * Initializes the lemmas HashMap and reads the lemmatization list from a CSV file.
     */
    public Lemmatizer() {
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:C:/Users/Braden Zingler/Desktop/Summer projects/search_engine/lemmas.db");
            System.out.println("Connection to lemmas db has been established.");
            // We only need to run this once to read the lemmatization list into the database
            // readLemmaList();
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

    
    /**
     * Reads the lemmatizations_list.csv file into a SQLite table when data is updated.
     * This allows for constant time lookups and lemmatization of each keyword.
     */
    public void readLemmaList() {
        String addUrl = "INSERT OR IGNORE INTO lemmas (key, val) VALUES (?, ?)";
        try (PreparedStatement pstm = conn.prepareStatement(addUrl)){
            conn.setAutoCommit(false);
            File f = new File("crawler/src/main/resources/lemmatization_list.csv");
            try (Scanner scnr = new Scanner(f)) {
                while (scnr.hasNextLine()) { 
                    String line = scnr.nextLine();
                    String[] parts = line.split(",");
                    String key = parts[1].strip();
                    String val = parts[0].strip();
    
                    // insert into database
                    pstm.setString(1, key);
                    pstm.setString(2, val);
                    pstm.addBatch();
            }
            pstm.executeBatch();
            conn.commit();  
            System.out.println("Lemmas read into database successfully.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    } catch (SQLException e) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        e.printStackTrace();
    } 
    }


}
