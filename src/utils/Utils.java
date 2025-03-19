package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import model.*;
import adapter.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Order.class, new OrderTypeAdapter())
            .registerTypeAdapter(CartItem.class, new CartItemTypeAdapter())
            .create();

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody()))) {
            return br.lines().collect(Collectors.joining());
        }
    }

    public static Map<String, String> parseJsonBody(String json) {
        return gson.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
    }

    public static Map<String, Object> parseComplexJsonBody(String json) {
        return gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
    }

    public static Map<String, String> parseQueryString(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    result.put(pair[0], pair[1]);
                }
            }
        }
        return result;
    }

    public static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    public static String collectionToJson(Collection<?> items) {
        return gson.toJson(items);
    }
}