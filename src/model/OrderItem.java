package model;

public class OrderItem {
    private int productId;
    private int quantity;
    private double price;

    public OrderItem(int productId, int quantity, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    public String toJson() {
        return String.format(
                "{\"productId\":%d,\"quantity\":%d,\"price\":%.2f}",
                productId, quantity, price
        );
    }
}