package server;

import com.sun.net.httpserver.HttpServer;
import handler.*;
import service.*;
import utils.JsonDataManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class ECommerceServer {
    private static final int PORT = 8000;
    private HttpServer server;
    private JsonDataManager dataManager;
    private ProductService productService;
    private OrderService orderService;
    private CartService cartService;
    private UserService userService;

    public ECommerceServer() throws IOException {
        // Create data directory if it doesn't exist
        Files.createDirectories(Paths.get("src/data"));

        // Initialize data manager and services
        dataManager = JsonDataManager.getInstance();
        productService = ProductService.getInstance();
        orderService = OrderService.getInstance();
        cartService = CartService.getInstance();
        userService = UserService.getInstance();

        // Initialize server with a larger backlog
        server = HttpServer.create(new InetSocketAddress(PORT), 100);

        // Set up routes with CORS handlers
        setupRoutes();

        // Use a thread pool with fixed size
        server.setExecutor(Executors.newFixedThreadPool(10));
    }

    private void setupRoutes() {
        // Wrap all handlers with CORS handler
        server.createContext("/", new CORSHandler(new RootHandler()));
        server.createContext("/api/products", new CORSHandler(new ProductHandler(productService)));
        server.createContext("/api/orders", new CORSHandler(new OrderHandler(orderService)));
        server.createContext("/api/cart", new CORSHandler(new CartHandler(cartService)));
        server.createContext("/api/users", new CORSHandler(new UserHandler(userService)));

        // Add admin routes
        server.createContext("/api/admin", new CORSHandler(
                new AdminHandler(productService, userService, orderService)
        ));
    }

    public void start() {
        server.start();
        System.out.println("\n=== E-Commerce Server Started ===");
        System.out.println("Server running on port: " + PORT);
        System.out.println("\nAvailable endpoints:");
        System.out.println("- http://localhost:" + PORT + "/");
        System.out.println("- http://localhost:" + PORT + "/api/products");
        System.out.println("- http://localhost:" + PORT + "/api/orders");
        System.out.println("- http://localhost:" + PORT + "/api/cart");
        System.out.println("- http://localhost:" + PORT + "/api/users");
        System.out.println("- http://localhost:" + PORT + "/api/admin");
        System.out.println("\nPress Ctrl+C to stop the server");
    }

    public static void main(String[] args) {
        try {
            ECommerceServer server = new ECommerceServer();
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

