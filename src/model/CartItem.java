package model;

public class CartItem {
    private final int productId;
    private volatile int quantity;
    private final double price;
    private final String productName;
    private final String imageUrl;

    // Constructor
    public CartItem(int productId, int quantity, double price, String productName, String imageUrl) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.productName = productName;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getProductName() { return productName; }
    public String getImageUrl() { return imageUrl; }

    // Setter for quantity
    public synchronized void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String toJson() {
        return String.format(
                "{\"productId\":%d,\"quantity\":%d,\"price\":%.2f," +
                        "\"productName\":\"%s\",\"imageUrl\":\"%s\"}",
                productId, quantity, price, productName, imageUrl
        );
    }
}