package model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class Product {
    private int id;
    private String name;
    private double price;
    private String description;
    private int stockQuantity;
    private String category;
    private String subcategory;
    private String imageUrl;
    private List<String> keywords; // Add keywords field

    public Product(int id, String name, double price, String description,
                   int stockQuantity, String category, String subcategory,
                   String imageUrl, List<String> keywords) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.subcategory = subcategory;
        this.imageUrl = imageUrl;
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public int getStockQuantity() { return stockQuantity; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setCategory(String category) { this.category = category; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public List<String> getKeywords() {
        return keywords;
    }

    public String toJson() {
        // Convert keywords list to JSON array string
        String keywordsJson = keywords.stream()
                .map(k -> "\"" + k + "\"")
                .collect(Collectors.joining(",", "[", "]"));

        return String.format(
                "{\"id\":%d,\"name\":\"%s\",\"price\":%.2f," +
                        "\"description\":\"%s\",\"stockQuantity\":%d," +
                        "\"category\":\"%s\",\"subcategory\":\"%s\"," +
                        "\"imageUrl\":\"%s\",\"keywords\":%s}",
                id, name, price, description, stockQuantity,
                category, subcategory, imageUrl, keywordsJson
        );
    }

    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', price=%.2f, category='%s'}",
                this.id, this.name, this.price, this.category);
    }

}