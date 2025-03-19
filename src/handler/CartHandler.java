package handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.CartService;
import model.Cart;
import utils.Utils;
import java.io.*;
import java.util.Map;

public class CartHandler implements HttpHandler {
    private final CartService cartService;

    public CartHandler(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int responseCode = 200;

        try {
            System.out.println("CartHandler: Received " + exchange.getRequestMethod() +
                    " request to " + exchange.getRequestURI());

            switch (exchange.getRequestMethod()) {
                case "GET" -> response = handleGet(exchange);
                case "POST" -> response = handlePost(exchange);
                case "DELETE" -> response = handleDelete(exchange);
                default -> {
                    response = "{\"error\": \"Method not supported\"}";
                    responseCode = 405;
                }
            }
        } catch (Exception e) {
            System.err.println("CartHandler error: " + e.getMessage());
            e.printStackTrace();
            response = "{\"error\": \"" + e.getMessage() + "\"}";
            responseCode = 500;
        }

        System.out.println("CartHandler response: " + response);
        Utils.sendResponse(exchange, response, responseCode);
    }

    private String handleGet(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            System.out.println("Cart request query: " + query);

            if (query == null) {
                System.out.println("Cart request: Missing query parameters");
                return "{\"error\": \"Missing userId parameter\"}";
            }

            Map<String, String> params = Utils.parseQueryString(query);
            System.out.println("Cart request params: " + params);

            String userIdStr = params.get("userId");
            if (userIdStr == null) {
                System.out.println("Cart request: Missing userId in parameters");
                return "{\"error\": \"Missing userId parameter\"}";
            }

            int userId = Integer.parseInt(userIdStr);
            System.out.println("Getting cart for userId: " + userId);

            Cart cart = cartService.getCart(userId);
            System.out.println("Retrieved cart: " + (cart != null ? cart.toJson() : "null"));

            if (cart == null) {
                return "{\"userId\":" + userId + ",\"items\":[],\"total\":0.0}";
            }

            return cart.toJson();
        } catch (Exception e) {
            System.err.println("Error in handleGet: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String handlePost(HttpExchange exchange) throws IOException {
        try {
            String requestBody = Utils.readRequestBody(exchange);
            System.out.println("Cart POST request body: " + requestBody);

            Map<String, String> data = Utils.parseJsonBody(requestBody);
            System.out.println("Parsed cart data: " + data);

            int userId = Integer.parseInt(data.get("userId"));
            int productId = Integer.parseInt(data.get("productId"));
            int quantity = Integer.parseInt(data.get("quantity"));

            System.out.println("Adding to cart - userId: " + userId +
                    ", productId: " + productId +
                    ", quantity: " + quantity);

            cartService.addToCart(userId, productId, quantity);
            Cart updatedCart = cartService.getCart(userId);
            System.out.println("Updated cart: " + updatedCart.toJson());

            return updatedCart.toJson();
        } catch (Exception e) {
            System.err.println("Error in handlePost: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private String handleDelete(HttpExchange exchange) {
        try {
            Map<String, String> params = Utils.parseQueryString(exchange.getRequestURI().getQuery());
            System.out.println("Cart DELETE params: " + params);

            int userId = Integer.parseInt(params.get("userId"));
            int productId = Integer.parseInt(params.get("productId"));

            System.out.println("Removing from cart - userId: " + userId +
                    ", productId: " + productId);

            cartService.removeFromCart(userId, productId);
            Cart updatedCart = cartService.getCart(userId);
            System.out.println("Updated cart after removal: " + updatedCart.toJson());

            return updatedCart.toJson();
        } catch (Exception e) {
            System.err.println("Error in handleDelete: " + e.getMessage());
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}