package SWBS.mediatracker;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class SWBCApplication {

    public static void main(String[] args) throws IOException {
        // Start simple HTTP server on port 8080
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(9999), 0);
        httpServer.createContext("/config", new ConfigHandler());
        httpServer.setExecutor(null);
        System.out.println("Listening on---- " + httpServer);
        httpServer.start();
    }

    static class ConfigHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                System.out.println("HttpServer Is Started--");
                InputStreamReader inputStreamReader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuffer  stringBuilder = new StringBuffer ();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(stringBuilder.toString());
                } catch (Exception e) {
                    System.err.println("Invalid JSON in request: " + e.getMessage());
                    String error = "Invalid JSON format.";
                    exchange.sendResponseHeaders(400, error.length());
                    exchange.getResponseBody().write(error.getBytes());
                    exchange.close();
                    return;
                }
                System.out.println("HttpServer Is line--" + jsonObject);
                String baseDomain = jsonObject.getString("baseDomain");
                String tokenPath = jsonObject.getString("tokenPath");
                String fullUrl = baseDomain + tokenPath;
                System.out.println("Full Url Is " + fullUrl);
                try {
                    URL url = new URL(fullUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    int responseCode = conn.getResponseCode();

                    InputStream responseStream = (responseCode == 200)
                            ? conn.getInputStream()
                            : conn.getErrorStream();

                    BufferedReader readerUrl = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8));
                    StringBuilder responseBuilder = new StringBuilder();
                    String lineUrl;
                    while ((lineUrl = readerUrl.readLine()) != null) {
                        responseBuilder.append(lineUrl);
                    }

                    String responseBody = responseBuilder.toString();
                    if (responseCode == 200) {
                        JSONObject json = new JSONObject(responseBody);
                        String token = json.getString("access_token");
                        int expires = json.getInt("expires_in");
                        String type = json.getString("token_type");

                    } else {
                        System.err.println(" Failed to get token: " + responseBody);
                    }

                    System.out.println("HTTP Status Code: " + responseCode);
                    System.out.println("Response Body: \n" + responseBuilder.toString());
                } catch (IOException e) {
                    System.err.println("Request failed: " + e.getMessage());
                }
            } else {
                String error = "Only POST method is supported.";
                exchange.sendResponseHeaders(405, error.length());
                OutputStream os = exchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
            }
        }


    }
}
