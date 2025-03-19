package model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private List<CartItem> items;
    private ShippingAddress shippingAddress;
    private double total;
    private String status;
    private String orderDate;

    public Order(int id, int userId) {
        this.id = id;
        this.userId = userId;
        this.items = new ArrayList<>();
        this.total = 0.0;
        this.status = "PENDING";
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public List<CartItem> getItems() { return items; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public String getOrderDate() { return orderDate; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTotal(double total) { this.total = total; }
    public void setStatus(String status) { this.status = status; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setShippingAddress(ShippingAddress address) { this.shippingAddress = address; }

    public void addItem(CartItem item) {
        items.add(item);
        calculateTotal();
    }

    private void calculateTotal() {
        this.total = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public String toJson() {
        StringBuilder itemsJson = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) itemsJson.append(",");
            itemsJson.append(items.get(i).toJson());
        }
        itemsJson.append("]");

        String addressJson = shippingAddress != null ? shippingAddress.toJson() : "null";

        return String.format(
                "{\"id\":%d,\"userId\":%d,\"items\":%s," +
                        "\"total\":%.2f,\"status\":\"%s\",\"orderDate\":\"%s\"," +
                        "\"shippingAddress\":%s}",
                id, userId, itemsJson.toString(), total, status, orderDate, addressJson
        );
    }

    @Override
    public String toString() {
        return toJson();
    }
}