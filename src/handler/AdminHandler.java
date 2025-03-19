package handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.ProductService;
import service.UserService;
import service.OrderService;
import utils.Utils;
import java.io.*;
import java.util.Map;

public class AdminHandler implements HttpHandler {
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;

    public AdminHandler(ProductService productService, UserService userService, OrderService orderService) {
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int responseCode = 200;

        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (path.endsWith("/admin/users")) {
                response = handleUsers(exchange);
            } else if (path.endsWith("/admin/orders/all")) {
                response = handleOrders(exchange);
            } else if (path.endsWith("/products")) {
                if ("GET".equals(method)) {
                    response = handleGet(exchange);
                } else if ("POST".equals(method)) {
                    response = handlePost(exchange);
                } else if ("PUT".equals(method)) {
                    response = handlePut(exchange);
                } else if ("DELETE".equals(method)) {
                    response = handleDelete(exchange);
                } else {
                    response = "{\"error\": \"Method not supported\"}";
                    responseCode = 405;
                }
            } else {
                response = "{\"error\": \"Invalid endpoint\"}";
                responseCode = 404;
            }
        } catch (Exception e) {
            response = "{\"error\": \"" + e.getMessage() + "\"}";
            responseCode = 500;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(responseCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String handleUsers(HttpExchange exchange) {
        System.out.println("Handling users request...");
        String response = userService.getAllUsersWithRole("user");
        System.out.println("Users response: " + response);
        return response;
    }

    private String handleOrders(HttpExchange exchange) {
        System.out.println("Handling admin orders request...");
        String response = orderService.getAllOrders();
        System.out.println("Orders response: " + response);
        return response;
    }

    private String handleGet(HttpExchange exchange) {
        // Get products with pagination
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = Utils.parseQueryString(query);
        int page = Integer.parseInt(params.getOrDefault("page", "1"));
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "10"));
        return productService.getProductsPage(page, pageSize);
    }

    private String handlePost(HttpExchange exchange) throws IOException {
        // Add new product
        String body = new String(exchange.getRequestBody().readAllBytes());
        return productService.addProduct(body);
    }

    private String handlePut(HttpExchange exchange) throws IOException {
        // Update product
        String body = new String(exchange.getRequestBody().readAllBytes());
        return productService.updateProduct(body);
    }

    private String handleDelete(HttpExchange exchange) {
        // Delete product
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = Utils.parseQueryString(query);
        int productId = Integer.parseInt(params.get("id"));
        return productService.deleteProduct(productId);
    }
}