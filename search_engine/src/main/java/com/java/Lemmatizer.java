/**
 * A class that lemmatizes keywords using a pre-defined list of lemmatizations.
 * The lemmatizations are read from a CSV file and stored in a HashMap for constant time lookups.
 * The lemmatizer is used in the web scraping process to ensure that the keywords are in their base form.
 * Braden Zingler
 * 5/18/1014
 */
package com.java;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


public class Lemmatizer {
    public HashMap<String, String> lemmas;
    public HashMap<String, ArrayList<String>> synonsMap;

    /**
     * Constructor for the Lemmatizer class.
     * Initializes the lemmas HashMap and reads the lemmatization list from a CSV file.
     */
    public Lemmatizer() {
        this.lemmas = new HashMap<>();
        this.synonsMap = new HashMap<>();
        readLemmaList();
    }


    /**
     * Lemmatizes a keyword.
     * @param word the word to be lemmatized
     * @return the lemmatized version of the word
     */
    public String lemmatizeWord(String word) {
        if (this.lemmas.containsKey(word)) {
            return this.lemmas.get(word);
        }
        return word;
    }

    
    /**
     * Reads the lemmatizations_list.csv file into a HashMap before starting the web scraping.
     * This allows for constant time lookups and lemmatization on each keyword.
     */
    public void readLemmaList() {
        try {
            File f = new File("search_engine/src/main/resources/lemmatization_list.csv");
            Scanner scnr = new Scanner(f);
            while (scnr.hasNextLine()) { 
                String line = scnr.nextLine();
                String[] parts = line.split(",");
                this.lemmas.put(parts[1].strip(), parts[0].strip());
            }
            scnr.close();
            System.out.println("Lemmas read into hashmap successfully.");
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}
