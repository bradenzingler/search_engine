/**
 * TF-IDF Search Engine
 * Braden Zingler
 * tfidf(t, d, D) = tf(t, d) * idf(t, D)
 * tf = term frequency = n/N, n = # times a term appears in document, N = total num terms in document.
 * idf = inverse document frequency = log(N/n), N = # documents in data set, n = # documents containing the term
 */

package com.java;

import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) {
        final int port = 8080;
    
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = socket.accept();
                System.out.println("Client connected from " + clientSocket.getInetAddress());
                Thread thread = new Thread(new LocalHttpHandler(clientSocket));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}