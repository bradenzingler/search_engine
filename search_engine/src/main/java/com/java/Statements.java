/**
 * This class contains all the SQL statements that are used in the application.
 * Braden Zingler
 * 5/28/2024
 */
package com.java;


public class Statements {

    public static String GET_KEYWORD_URL_COUNTS = "SELECT COUNT(url) FROM url_keywords WHERE keyword = (?)";
    public static String CREATE_INDEX = "CREATE INDEX IF NOT EXISTS idx_keyword ON url_keywords (keyword)";
    public static String GET_DOCUMENTS_FOR_KEYWORD = "SELECT DISTINCT u.url, u.title, u.description, uk.tfidf "
            + "FROM url_keywords uk "
            + "JOIN keywords k ON uk.keyword_id = k.keyword_id "
            + "JOIN urls u ON u.url_id = uk.url_id "
            + "WHERE uk.keyword_id = ?";
    public static String GET_KEYWORD_ID = "SELECT keyword_id FROM keywords WHERE keyword = ?";

}
