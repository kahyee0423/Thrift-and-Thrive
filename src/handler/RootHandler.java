package handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import utils.Utils;
import java.io.IOException;

public class RootHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            String response = "{\"error\": \"Method not supported\"}";
            Utils.sendResponse(exchange, response, 405);
            return;
        }

        // Return API information
        String response = "{"
                + "\"message\": \"E-Commerce API Server\","
                + "\"version\": \"1.0\","
                + "\"endpoints\": ["
                + "  \"/api/products\","
                + "  \"/api/cart\","
                + "  \"/api/orders\","
                + "  \"/api/users\""
                + "],"
                + "\"status\": \"running\""
                + "}";

        Utils.sendResponse(exchange, response, 200);
    }
}