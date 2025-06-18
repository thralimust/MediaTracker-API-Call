package SWBS.mediatracker.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigHandler implements HttpHandler {

    private static final int MAX_RETRIES = 6;
    private static final int TOTAL_TIME_MS = 30_000;

    // Memory store to track how many times each extension was requested
    private static final Map<String, Integer> extensionAttempts = new ConcurrentHashMap<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String error = "Only POST method is supported.";
            exchange.sendResponseHeaders(405, error.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(error.getBytes());
            }
            return;
        }

        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery();
        JSONObject jsonResponse = new JSONObject();
        int statusCode;

        if (query == null || !query.contains("GENESYS_EXTENSION=")) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Missing GENESYS_EXTENSION parameter");
            statusCode = 400;
        } else {
            String value = "";
            String[] parts = query.split("GENESYS_EXTENSION=");
            if (parts.length > 1) {
                value = parts[1].trim();
            }
            if (value.isEmpty() || value.length() < 5) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Invalid GENESYS_EXTENSION value");
                statusCode = 400;
            } else {
                // Track how many times we've attempted this extension
                int currentTry = extensionAttempts.getOrDefault(value, 0) + 1;
                extensionAttempts.put(value, currentTry);

                long delay = TOTAL_TIME_MS / MAX_RETRIES;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (currentTry < MAX_RETRIES) {
                    String interactionId = UUID.randomUUID().toString();
                    jsonResponse.put("HTTP status Code", "success");
                    jsonResponse.put("extension", value);
                    jsonResponse.put("InteractionId", interactionId);
                    statusCode = 200;
                } else {
                    // On 6th time, return failure
                    String correlationId = UUID.randomUUID().toString();
                    String message = String.format("Unable retrieve action ConversationId for userId: %s after 6 tries. CorrelationId: %s", value, correlationId);
                    jsonResponse.put("status", "error");
                    jsonResponse.put("message", message);
                    statusCode = 404;

                    extensionAttempts.remove(value);
                }
            }
        }

        // Send JSON response
        byte[] responseBytes = jsonResponse.toString().getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
