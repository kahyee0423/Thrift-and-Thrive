package handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.UserService;
import model.User;
import utils.JsonDataManager;
import utils.Utils;
import java.io.*;
import java.util.Map;

public class UserHandler implements HttpHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int responseCode = 200;

        try {
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(exchange.getRequestMethod()) && path.startsWith("/api/users/details/")) {
                String email = path.substring("/api/users/details/".length());
                response = handleGetUserDetails(email);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                Map<String, String> data = Utils.parseJsonBody(body);

                if ("/api/users/signin".equals(path)) {
                    response = handleSignIn(data);
                } else if ("/api/users/signup".equals(path)) {
                    response = handleSignUp(data);
                } else if ("/api/users/reset-password".equals(path)) {
                    response = handleResetPassword(data);
                } else {
                    responseCode = 404;
                    response = "{\"error\": \"Invalid endpoint\"}";
                }
            } else {
                responseCode = 405;
                response = "{\"error\": \"Method not supported\"}";
            }
        } catch (Exception e) {
            responseCode = 500;
            response = "{\"error\": \"" + e.getMessage() + "\"}";
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(responseCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String handleSignIn(Map<String, String> data) {
        User user = userService.signIn(data.get("email"), data.get("password"));
        return user.toJson();
    }

    private String handleSignUp(Map<String, String> data) {
        User user = userService.createAccount(
                data.get("email"),
                data.get("password"),
                "user"  // Default role
        );
        return user.toJson();
    }

    private String handleForgotPassword(Map<String, String> data) {
        userService.initiatePasswordReset(data.get("email"));
        return "{\"message\":\"Password reset instructions sent to your email\"}";
    }

    private String handleResetPassword(Map<String, String> data) {
        try {
            String email = data.get("email");
            String newPassword = data.get("newPassword");

            if (email == null || newPassword == null) {
                throw new RuntimeException("Email and new password are required");
            }

            boolean success = userService.resetPassword(email, newPassword);
            if (success) {
                return "{\"message\": \"Password reset successful\"}";
            } else {
                throw new RuntimeException("Failed to reset password");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error resetting password: " + e.getMessage());
        }
    }

    private String handleGetUserDetails(String email) {
        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            return JsonDataManager.getInstance().getGson().toJson(user);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user details: " + e.getMessage());
        }
    }
}