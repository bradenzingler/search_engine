/**
 * Query.java
 * This class is responsible for handling the user query.
 * It cleans and filters the query, and generates the term frequency vector.
 * Braden Zingler
 */


package com.java;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Query {

    private String rawText;                 // The raw text of the query before filtering
    private Lemmatizer lem;                 // The lemmatizer object used to lemmatize the query
    private List<String> queryKeywords;     // The resulting list of query keywords


    /**
     * Constructor for the Query class
     * @param rawText The raw text of the query
     * @param lem The lemmatizer object
     */
    public Query(String rawText, Lemmatizer lem) {
        this.rawText = rawText;
        this.lem = lem;
        this.queryKeywords = this.cleanAndFilterQuery();
    }

    
    /**
     * Getter method for the query keywords.
     * @return the list of query keywords.
     */
    public List<String> getQueryKeywords() {
        return this.queryKeywords;
    }


    /**
     * Cleans out the user query by filtering and parsing it.
     * @return The cleaned and filtered query
     */
    public List<String> cleanAndFilterQuery() {
        String[] queryWords = this.rawText.split("\\s+");
        return filterKeywords(Arrays.asList(queryWords));
    }


    /**
     * Filter out the stopwords from the list of words
     * @param words The list of words
     * @return The list of words without the stopwords
     */
    private List<String> filterKeywords(List<String> words) {
        try {
            List<String> filteredKeywords = new ArrayList<>();
            for (String word : words) {
                if (!isStopword(word)) {
                    String newWord = this.lem.lemmatizeWord(word.toLowerCase());
                    filteredKeywords.add(newWord);
                }
            }
            System.out.println("Filtered keywords: " + filteredKeywords.toString());
            return filteredKeywords;
        } catch (Exception e) {
            System.out.println("Error while filtering keywords: " + e);
        }
        return null;
    }


    /**
     * Check if a word is a stopword.
     * @param word The word to check
     * @return True if the word is a stopword, false otherwise
     */
    private static boolean isStopword(String word) {
        try {
            String[] stopWords = new String[] {"a","about","above","according","across","actually","after","again","against","all","almost","also","although","always","am","among","amongst","an","and","any","anything","anyway","are","as","at","be","became","become","because","been","before","being","below","between","both","but","by","can","could","did","do","does","doing","down","during","each","either","else","few","for","from","further","had","has","have","having","he","he'd","he'll","hence","he's","her","here","here's","hers","herself","him","himself","his","how","how's","I","I'd","I'll","I'm","I've","if","in","into","is","it","it's","its","itself","just","let's","may","maybe","me","might","mine","more","most","must","my","myself","neither","nor","not","of","oh","on","once","only","ok","or","other","ought","our","ours","ourselves","out","over","own","same","she","she'd","she'll","she's","should","so","some","such","than","that","that's","the","their","theirs","them","themselves","then","there","there's","these","they","they'd","they'll","they're","they've","this","those","through","to","too","under","until","up","very","was","we","we'd","we'll","we're","we've","were","what","what's","when","whenever","when's","where","whereas","wherever","where's","whether","which","while","who","whoever","who's","whose","whom","why","why's","will","with","within","would","yes","yet","you","you'd","you'll","you're","you've","your","yours","yourself","yourselves"};
            List<String> stopWordsList = Arrays.asList(stopWords);
            if (stopWordsList.contains(word.toLowerCase())) return true;
            return false;
        } catch (Exception e) {
            System.out.println("An error occurred while checking if is stopword: " + e);
        }
        return true;
    }
}
