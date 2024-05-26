package com.java;

public class Statements {

public static String GET_KEYWORD_URL_COUNTS = "SELECT COUNT(url) FROM url_keywords WHERE keyword = (?)";
public static String CREATE_INDEX = "CREATE INDEX IF NOT EXISTS idx_keyword ON url_keywords (keyword)";
public static String GET_DOCUMENTS_FOR_KEYWORD = "SELECT tfidf, url, title, description FROM url_keywords WHERE keyword = ? LIMIT 10";
    
}
