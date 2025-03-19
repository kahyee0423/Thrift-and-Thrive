package service;

import model.Product;
import utils.JsonDataManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProductService {
    private static ProductService instance;
    private final JsonDataManager dataManager;

    private ProductService() {
        this.dataManager = JsonDataManager.getInstance();
    }

    public static ProductService getInstance() {
        if (instance == null) {
            instance = new ProductService();
        }
        return instance;
    }

    public Collection<Product> getAllProducts() {
        return dataManager.getAllProducts();
    }

    public Product getProduct(int id) {
        return dataManager.getProduct(id);
    }

    public void addProduct(Product product) {
        dataManager.saveProduct(product);
    }

    public Collection<Product> getProductsByCategory(String category) {
        return getAllProducts().stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    public String getProductsPage(int page, int pageSize) {
        List<Product> allProducts = new ArrayList<>(dataManager.getAllProducts());
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allProducts.size());

        if (start >= allProducts.size()) {
            return "[]";
        }

        List<Product> pagedProducts = allProducts.subList(start, end);
        return dataManager.getGson().toJson(pagedProducts);
    }

    public String addProduct(String productJson) {
        Product product = dataManager.getGson().fromJson(productJson, Product.class);
        dataManager.saveProduct(product);
        return product.toJson();
    }

    public String updateProduct(String productJson) {
        Product product = dataManager.getGson().fromJson(productJson, Product.class);
        if (product.getId() == 0) {
            throw new RuntimeException("Product ID is required for update");
        }
        dataManager.saveProduct(product);
        return product.toJson();
    }

    public String deleteProduct(int productId) {
        Product product = dataManager.getProduct(productId);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        dataManager.deleteProduct(productId);
        return "{\"message\":\"Product deleted successfully\"}";
    }
}