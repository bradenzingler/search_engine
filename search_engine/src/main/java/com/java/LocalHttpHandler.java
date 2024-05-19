package com.java;

import java.net.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class LocalHttpHandler implements Runnable {

    private final Socket clientSocket;
    private final File htmlFile;
    private final File cssFile;

    public LocalHttpHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.htmlFile = new File("search_engine/src/main/resources/index.html");
        this.cssFile = new File("search_engine/src/main/resources/style.css");
    }

    @Override
    public void run() {
       SearchEngine engine = new SearchEngine();

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            String inputLine;
            boolean isGetRequest = false;
            String query = null;

            while ((inputLine = in.readLine()) != null) {
                if (inputLine.isEmpty()) break;
                //System.out.println("Received: " + inputLine);

                if (inputLine.startsWith("GET")) {
                    isGetRequest = true;
                    if (inputLine.contains("=")) {
                        query = parseQuery(inputLine);
                    }
                }
            }

            if (isGetRequest) {
                String response = "HTTP/1.1 200 OK\r\n"
                                + "Content-Type: text/html\r\n"
                                + "\r\n";
                out.write(response);
                
                StringBuilder html = new StringBuilder();
                try (Scanner htmlScanner = new Scanner(htmlFile);
                     Scanner cssScanner = new Scanner(cssFile)) {

                    while (htmlScanner.hasNextLine()) {
                        html.append(htmlScanner.nextLine()).append("\n");
                    }

                    html.append("<style>");
                    while (cssScanner.hasNextLine()) {
                        html.append(cssScanner.nextLine()).append("\n");
                    }
                    html.append("</style>");
                }

                String htmlOutput = html.toString().replace("${query}", query == null ? "" : query);
                out.write(htmlOutput);

                if (query != null) {
                    System.out.println("Searching for: " + query);
                    Long t0 = System.currentTimeMillis();
                    List<List<String>> results = engine.search(query);
                    Long t1 = System.currentTimeMillis();
                    Long time = t1-t0;

                    out.write("<p id='time-stats'>"+results.size()+" results in " + time + " ms</p>");
                    out.write("<ul>");
                    for (List<String> result : results) {
                        String url = result.get(0);
                        String title = result.get(1);
                        out.write("<li>"+"<p><a href=\"" + url + "\">" + title + "</a></p>" + "</li>");
                    }
                    // for (Map<String, String[]> metadata : results) {
                    //     out.write("<li>"+"<p><a href=\"" + metadata.get("url") + "\">" 
                    //     + metadata.get("title") + "</a></p>" + "<br><p>" 
                    //     + metadata.get("description") + "</p></li>");
                    // }
                    out.write("</ul>");
                }

                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public String parseQuery(String inputLine) {
        String[] parts = inputLine.split(" ");
        if (parts.length > 1) {
            String[] queryParts = parts[1].split("\\?");
            if (queryParts.length > 1) {
                String[] keyValue = queryParts[1].split("=");
                if (keyValue.length > 1) {
                    try {
                        return URLDecoder.decode(keyValue[1], "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
