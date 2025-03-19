package handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.ProductService;
import model.Product;
import utils.Utils;
import java.io.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;

public class ProductHandler implements HttpHandler {
    private final ProductService productService;

    public ProductHandler(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int responseCode = 200;

        try {
            switch (exchange.getRequestMethod()) {
                case "GET" -> response = handleGet(exchange);
                case "POST" -> response = handlePost(exchange);
                default -> {
                    response = "{\"error\": \"Method not supported\"}";
                    responseCode = 405;
                }
            }
        } catch (Exception e) {
            response = "{\"error\": \"" + e.getMessage() + "\"}";
            responseCode = 500;
            e.printStackTrace();
        }

        Utils.sendResponse(exchange, response, responseCode);
    }

    private String handleGet(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = Utils.parseQueryString(query);

            // Handle specific product request
            if (params.containsKey("id")) {
                int id = Integer.parseInt(params.get("id"));
                Product product = productService.getProduct(id);
                if (product == null) {
                    return "{\"error\": \"Product not found\"}";
                }
                return product.toJson();
            }

            // Handle filtered products request
            Collection<Product> products = productService.getAllProducts();

            // Apply category filter if present
            if (params.containsKey("category")) {
                String category = params.get("category");
                products = products.stream()
                        .filter(p -> p.getCategory().equalsIgnoreCase(category))
                        .collect(Collectors.toList());
            }

            // Apply keyword search if present
            if (params.containsKey("keywords")) {
                String keyword = params.get("keywords").toLowerCase();
                products = products.stream()
                        .filter(p -> matchesSearch(p, keyword))
                        .collect(Collectors.toList());
            }

            // Apply pagination if present
            if (params.containsKey("page") && params.containsKey("limit")) {
                int page = Integer.parseInt(params.get("page"));
                int limit = Integer.parseInt(params.get("limit"));
                int skip = (page - 1) * limit;

                products = products.stream()
                        .skip(skip)
                        .limit(limit)
                        .collect(Collectors.toList());
            }

            // Convert to JSON array
            StringBuilder jsonArray = new StringBuilder("[");
            Iterator<Product> iterator = products.iterator();
            while (iterator.hasNext()) {
                jsonArray.append(iterator.next().toJson());
                if (iterator.hasNext()) {
                    jsonArray.append(",");
                }
            }
            jsonArray.append("]");

            return jsonArray.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    // In ProductHandler
    private String handlePost(HttpExchange exchange) throws IOException {
        String requestBody = Utils.readRequestBody(exchange);
        Map<String, Object> productData = Utils.parseComplexJsonBody(requestBody);

        @SuppressWarnings("unchecked")
        List<String> keywords = productData.containsKey("keywords")
                ? (List<String>) productData.get("keywords")
                : new ArrayList<>();

        Product product = new Product(
                0,
                (String) productData.get("name"),
                ((Number) productData.get("price")).doubleValue(),
                (String) productData.get("description"),
                ((Number) productData.get("stockQuantity")).intValue(),
                (String) productData.get("category"),
                (String) productData.get("subcategory"),
                (String) productData.get("imageUrl"),
                keywords
        );

        productService.addProduct(product);
        return "{\"message\": \"Product added successfully\", \"productId\": " + product.getId() + "}";
    }

    private void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(code, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private boolean matchesSearch(Product product, String searchTerm) {
        // Check if search term matches name, description, or any keyword
        return product.getName().toLowerCase().contains(searchTerm) ||
                product.getDescription().toLowerCase().contains(searchTerm) ||
                product.getKeywords().stream()
                        .anyMatch(keyword -> keyword.toLowerCase().contains(searchTerm));
    }
}
