package service;

import com.google.gson.reflect.TypeToken;
import model.*;
import utils.JsonDataManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;
import java.lang.reflect.Type;

public class OrderService {
    private static OrderService instance;
    private final JsonDataManager dataManager;
    private final CartService cartService;
    private static final String ORDERS_FILE = "orders.json";
    private static final Type ORDER_LIST_TYPE = new TypeToken<ArrayList<Order>>(){}.getType();

    private OrderService() {
        this.dataManager = JsonDataManager.getInstance();
        this.cartService = CartService.getInstance();
    }

    public static OrderService getInstance() {
        if (instance == null) {
            synchronized (OrderService.class) {
                if (instance == null) {
                    instance = new OrderService();
                }
            }
        }
        return instance;
    }

    public Order createOrder(int userId) {
        try {
            // Get the user's cart
            Cart userCart = cartService.getCart(userId);
            System.out.println("\n=== Creating Order from Cart ===");
            System.out.println("User Cart: " + userCart.toJson());

            // Get valid items (quantity > 0) from cart
            Collection<CartItem> cartItems = userCart.getItems();
            List<CartItem> validItems = cartItems.stream()
                    .filter(item -> item.getQuantity() > 0)
                    .toList();

            if (validItems.isEmpty()) {
                throw new IllegalStateException("No valid items in cart");
            }

            // Create new order
            Order order = new Order(dataManager.getNextOrderId(), userId);
            order.setOrderDate(LocalDateTime.now().toString());
            order.setStatus("PENDING");

            // Add valid items to order
            double total = 0.0;
            for (CartItem cartItem : validItems) {
                CartItem orderItem = new CartItem(
                        cartItem.getProductId(),
                        cartItem.getQuantity(),
                        cartItem.getPrice(),
                        cartItem.getProductName(),
                        cartItem.getImageUrl()
                );
                order.addItem(orderItem);
                total += cartItem.getPrice() * cartItem.getQuantity();
            }
            order.setTotal(Math.round(total * 100.0) / 100.0);

            System.out.println("Created order with items: " + order.toJson());

            // Save order
            dataManager.saveOrder(order);
            System.out.println("Saved order to database");

            // Clear cart after successful order creation
            cartService.clearCart(userId);
            System.out.println("Cleared user cart");

            return order;
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Order getOrder(int orderId) {
        return dataManager.getOrder(orderId);
    }

    public Collection<Order> getUserOrders(int userId) {
        String json = dataManager.readFile(ORDERS_FILE);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Collection<Order> allOrders = dataManager.getGson().fromJson(json, ORDER_LIST_TYPE);
        return allOrders.stream()
                .filter(order -> order.getUserId() == userId)
                .collect(Collectors.toList());
    }

    public void updateOrder(Order order) {
        System.out.println("Updating order: " + order.toJson());
        dataManager.saveOrder(order);
    }

    public String getAllOrders() {
        Collection<Order> orders = dataManager.getAllOrders();
        return dataManager.getGson().toJson(orders);
    }
}