package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.*;
import adapter.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class JsonDataManager {
    private static volatile JsonDataManager instance;
    private final Gson gson;
    private static final String DATA_DIR = "C:\\Users\\User\\IdeaProjects\\c_backend\\src\\data";

    // Thread-safe maps
    private final ConcurrentHashMap<Integer, Product> products;
    private final ConcurrentHashMap<Integer, User> users;
    private final ConcurrentHashMap<Integer, Cart> carts;
    private final ConcurrentHashMap<Integer, Order> orders;

    // ID generators
    private final AtomicInteger productIdGenerator;
    private final AtomicInteger userIdGenerator;
    private final AtomicInteger orderIdGenerator;

    private JsonDataManager() {
        // Initialize Gson with type adapters
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Cart.class, new CartTypeAdapter())
                .registerTypeAdapter(CartItem.class, new CartItemTypeAdapter())
                .create();

        products = new ConcurrentHashMap<>();
        users = new ConcurrentHashMap<>();
        carts = new ConcurrentHashMap<>();
        orders = new ConcurrentHashMap<>();

        // Initialize ID generators
        productIdGenerator = new AtomicInteger(0);
        userIdGenerator = new AtomicInteger(0);
        orderIdGenerator = new AtomicInteger(0);

        loadAllData();
        initializeIdGenerators();
    }

    // Thread-safe singleton pattern
    public static JsonDataManager getInstance() {
        if (instance == null) {
            synchronized (JsonDataManager.class) {
                if (instance == null) {
                    instance = new JsonDataManager();
                }
            }
        }
        return instance;
    }

    private void initializeIdGenerators() {
        // Set ID generators to max current ID + 1
        productIdGenerator.set(products.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1);

        userIdGenerator.set(users.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1);

        orderIdGenerator.set(orders.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1);
    }

    private void loadAllData() {
        try {
            System.out.println("Loading all data from directory: " + DATA_DIR);
            Files.createDirectories(Paths.get(DATA_DIR));

            System.out.println("Loading products...");
            loadProducts();
            System.out.println("Loaded products: " + products.size());

            System.out.println("Loading users...");
            loadUsers();
            System.out.println("Loaded users: " + users.size());

            System.out.println("Loading carts...");
            loadCarts();
            System.out.println("Loaded carts: " + carts.size());

            System.out.println("Loading orders...");
            loadOrders();
            System.out.println("Loaded orders: " + orders.size());
        } catch (IOException e) {
            System.err.println("Error in loadAllData");
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        List<Product> productList = loadData("products.json", new TypeToken<List<Product>>(){}.getType());
        products.clear();
        productList.forEach(p -> products.put(p.getId(), p));
    }

    private void loadUsers() {
        List<User> userList = loadData("users.json", new TypeToken<List<User>>(){}.getType());
        System.out.println("Loaded Users: " + users);
        users.clear();
        userList.forEach(u -> users.put(u.getId(), u));
    }

    private void loadCarts() {
        List<Cart> cartList = loadData("carts.json", new TypeToken<List<Cart>>(){}.getType());
        System.out.println("Loaded carts: " + cartList); // Debugging line
        carts.clear();
        cartList.forEach(c -> carts.put(c.getUserId(), c));
    }

    private void loadOrders() {
        System.out.println("Loading orders from file...");
        try {
            Path filePath = Paths.get(DATA_DIR, "orders.json");
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                saveData("orders.json", new ArrayList<>());
            }

            String content = new String(Files.readAllBytes(filePath));
            System.out.println("Orders file content: " + content);

            if (content.trim().isEmpty()) {
                content = "[]";
            }

            List<Order> orderList = gson.fromJson(content, new TypeToken<List<Order>>(){}.getType());
            System.out.println("Parsed orders: " + orderList);

            orders.clear();
            if (orderList != null) {
                for (Order order : orderList) {
                    if (order != null && order.getId() > 0) {
                        orders.put(order.getId(), order);
                        System.out.println("Added order to map: " + order.getId());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Final orders map: " + orders);
    }

    private <T> List<T> loadData(String filename, Type type) {
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            System.out.println("Loading data from: " + filePath);

            if (!Files.exists(filePath)) {
                System.out.println("File does not exist: " + filePath);
                Files.createFile(filePath);
                saveData(filename, new ArrayList<>());
                return new ArrayList<>();
            }

            String jsonContent = new String(Files.readAllBytes(filePath));
            System.out.println("File content for " + filename + ": " + jsonContent);

            if (jsonContent.trim().isEmpty()) {
                System.out.println("File is empty, returning empty list");
                return new ArrayList<>();
            }

            List<T> data = gson.fromJson(jsonContent, type);
            System.out.println("Parsed data: " + data);
            return data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error loading data from " + filename);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private synchronized void saveData(String filename, Collection<?> data) {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Path filePath = Paths.get(DATA_DIR, filename);
            System.out.println("Saving data to: " + filePath);

            String json = gson.toJson(data);
            Files.write(filePath, json.getBytes());

            System.out.println("Successfully saved data to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving data to " + filename);
            e.printStackTrace();
        }
    }

    // Thread-safe product methods
    public Collection<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public Product getProduct(int id) {
        return products.get(id);
    }

    public void saveProduct(Product product) {
        if (product.getId() == 0) {
            product.setId(productIdGenerator.getAndIncrement());
        }
        products.put(product.getId(), product);
        saveData("products.json", products.values());
    }

    // Thread-safe user methods
    public Collection<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User getUser(int id) {
        return users.get(id);
    }

    public void saveUser(User user) {
        users.put(user.getId(), user);
        saveData("users.json", new ArrayList<>(users.values()));
    }

    // Thread-safe cart methods
    public Cart getCart(int userId) {
        return carts.computeIfAbsent(userId, id -> new Cart(id));
    }

    public void saveCart(Cart cart) {
        carts.put(cart.getUserId(), cart);
        saveData("carts.json", carts.values());
    }

    // Thread-safe order methods
    public Collection<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public Order getOrder(int orderId) {
        return orders.get(orderId);
    }

    public List<Order> getUserOrders(int userId) {
        return orders.values().stream()
                .filter(order -> order.getUserId() == userId)
                .collect(Collectors.toList());
    }

    public void saveOrder(Order order) {
        if (order.getId() == 0) {
            order.setId(orderIdGenerator.getAndIncrement());
        }
        orders.put(order.getId(), order);
        saveData("orders.json", new ArrayList<>(orders.values()));
    }

    public Gson getGson() {
        return gson;
    }

    public Map<Integer, User> getUsers() {
        return users;
    }

    // Utility methods
    public int getNextProductId() {
        return productIdGenerator.get();
    }

    public int getNextUserId() {
        return userIdGenerator.get();
    }

    public int getNextOrderId() {
        return orderIdGenerator.get();
    }

    public void deleteProduct(int productId) {
        products.remove(productId);
        saveData("products.json", products.values());
    }

    /*private synchronized void saveData(String filename, Object data) {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Path filePath = Paths.get(DATA_DIR, filename);
            System.out.println("Saving data to: " + filePath);

            String json;
            if (data instanceof String) {
                json = (String) data;
            } else if (data instanceof Collection) {
                json = gson.toJson(data);
            } else {
                json = gson.toJson(data);
            }

            Files.write(filePath, json.getBytes());
            System.out.println("Successfully saved data to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving data to " + filename);
            e.printStackTrace();
        }
    }*/

    public String getAllUsersWithRole(String role) {
        return gson.toJson(
                users.values().stream()
                        .filter(user -> role.equals(user.getRole()))
                        .collect(Collectors.toList())
        );
    }

    public String getAllOrdersJson() {
        return gson.toJson(new ArrayList<>(orders.values()));
    }

    public String readFile(String filename) {
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            if (!Files.exists(filePath)) {
                return "";
            }
            return new String(Files.readAllBytes(filePath));
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
            return "";
        }
    }

    public void writeFile(String filename, String content) {
        try {
            Path filePath = Paths.get(DATA_DIR, filename);
            Files.write(filePath, content.getBytes());
        } catch (IOException e) {
            System.err.println("Error writing to file " + filename + ": " + e.getMessage());
        }
    }

    public void appendToFile(String filename, String content) {
        try {
            String filePath = DATA_DIR + filename;
            if (!Files.exists(Paths.get(filePath))) {
                writeFile(filename, "[]");
            }

            String existingContent = readFile(filename);
            if (existingContent.equals("[]")) {
                writeFile(filename, "[" + content + "]");
            } else {
                String newContent = existingContent.substring(0, existingContent.length() - 1)
                        + "," + content + "]";
                writeFile(filename, newContent);
            }
        } catch (Exception e) {
            System.err.println("Error appending to file " + filename + ": " + e.getMessage());
        }
    }

    public void deleteFromFile(String filename, int id) {
        try {
            String content = readFile(filename);
            if (content.isEmpty()) return;

            // Parse the JSON array, remove the item, and write back
            // This is a simplified version - you might want to implement specific deletion logic
            String newContent = content.replaceAll(
                    "\\{[^}]*\"id\"\\s*:\\s*" + id + "[^}]*\\},?", ""
            ).replaceAll("\\[\\s*,", "[").replaceAll(",\\s*\\]", "]");

            writeFile(filename, newContent);
        } catch (Exception e) {
            System.err.println("Error deleting from file " + filename + ": " + e.getMessage());
        }
    }
}