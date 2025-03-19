package handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import model.Order;
import model.ShippingAddress;
import service.OrderService;
import utils.Utils;
import adapter.OrderTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class OrderHandler implements HttpHandler {
    private final OrderService orderService;
    private final Gson gson;

    public OrderHandler(OrderService orderService) {
        this.orderService = orderService;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Order.class, new OrderTypeAdapter())
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int responseCode = 200;

        // Add CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        // Handle preflight requests
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(exchange.getRequestMethod())) {
                if (path.startsWith("/api/orders/user/")) {
                    String userId = path.substring("/api/orders/user/".length());
                    response = handleGetUserOrders(Integer.parseInt(userId));
                } else if (path.endsWith("/admin/orders/all")) {
                    response = orderService.getAllOrders();
                } else {
                    responseCode = 404;
                    response = "{\"error\": \"Invalid endpoint\"}";
                }
            } else if ("POST".equals(exchange.getRequestMethod())) {
                response = handlePost(exchange);
            } else {
                responseCode = 405;
                response = "{\"error\": \"Method not supported\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = "{\"error\": \"" + e.getMessage() + "\"}";
            responseCode = 500;
        }

        Utils.sendResponse(exchange, response, responseCode);
    }

    private String handlePost(HttpExchange exchange) throws IOException {
        String requestBody = Utils.readRequestBody(exchange);
        System.out.println("\n=== Creating New Order ===");
        System.out.println("Received order request body: " + requestBody);

        try {
            Map<String, Object> orderData = Utils.parseComplexJsonBody(requestBody);
            int userId = ((Number) orderData.get("userId")).intValue();
            System.out.println("Creating order for userId: " + userId);

            Order order = orderService.createOrder(userId);
            System.out.println("Created order from cart: " + order.toJson());

            if (orderData.containsKey("shippingAddress")) {
                Map<String, String> addressData = (Map<String, String>) orderData.get("shippingAddress");
                ShippingAddress shippingAddress = new ShippingAddress(
                        addressData.get("fullName"),
                        addressData.get("email"),
                        addressData.get("phone"),
                        addressData.get("address"),
                        addressData.get("city"),
                        addressData.get("state"),
                        addressData.get("postalCode")
                );
                order.setShippingAddress(shippingAddress);
                orderService.updateOrder(order);
                System.out.println("Updated order with shipping: " + order.toJson());
            }

            String jsonResponse = gson.toJson(order);
            System.out.println("Sending response: " + jsonResponse);
            return jsonResponse;
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to create order: " + e.getMessage());
        }
    }

    private String handleGetUserOrders(int userId) {
        try {
            System.out.println("Fetching orders for userId: " + userId);
            Collection<Order> userOrders = orderService.getUserOrders(userId);
            System.out.println("Found orders: " + userOrders.size());
            String jsonResponse = gson.toJson(userOrders);
            System.out.println("Sending response: " + jsonResponse);
            return jsonResponse;
        } catch (Exception e) {
            System.err.println("Error fetching user orders: " + e.getMessage());
            throw new RuntimeException("Error fetching user orders: " + e.getMessage());
        }
    }
}