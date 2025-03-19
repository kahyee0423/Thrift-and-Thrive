package model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Cart {
    private final int userId;
    private final Map<Integer, CartItem> items;
    private volatile double total;

    public Cart(int userId) {
        this.userId = userId;
        this.items = new ConcurrentHashMap<>();
        this.total = 0.0;
    }

    // Add this getter for JSON deserialization
    public int getUserId() {
        return userId;
    }

    public Collection<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public double getTotal() {
        return total;
    }

    // Use this for JSON deserialization
    public void setItems(List<CartItem> itemList) {
        items.clear();
        if (itemList != null) {
            for (CartItem item : itemList) {
                items.put(item.getProductId(), item);
            }
        }
        calculateTotal();
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public synchronized void addItem(CartItem item) {
        items.put(item.getProductId(), item);
        calculateTotal();
    }

    private synchronized void calculateTotal() {
        this.total = items.values().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public String toJson() {
        StringJoiner itemsJson = new StringJoiner(",", "[", "]");
        items.values().forEach(item -> itemsJson.add(item.toJson()));

        return String.format(
                "{\"userId\":%d,\"items\":%s,\"total\":%.2f}",
                userId, itemsJson.toString(), total
        );
    }

    @Override
    public String toString() {
        return toJson();
    }

    public synchronized void removeItem(int productId) {
        items.remove(productId);
        calculateTotal();
    }

    public synchronized void updateItemQuantity(int productId, int quantity) {
        CartItem item = items.get(productId);
        if (item != null) {
            item.setQuantity(quantity);
            calculateTotal();
        }
    }

    public synchronized void clearItems() {
        items.clear();
        total = 0.0;
    }
}

