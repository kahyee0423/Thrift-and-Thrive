package service;

import model.Cart;
import model.CartItem;
import model.Product;
import utils.JsonDataManager;

public class CartService {
    private static CartService instance;
    private final JsonDataManager dataManager;
    private final ProductService productService;

    private CartService() {
        this.dataManager = JsonDataManager.getInstance();
        this.productService = ProductService.getInstance();
    }

    public static CartService getInstance() {
        if (instance == null) {
            synchronized (CartService.class) {
                if (instance == null) {
                    instance = new CartService();
                }
            }
        }
        return instance;
    }

    public Cart getCart(int userId) {
        Cart cart = dataManager.getCart(userId);
        System.out.println("Retrieved cart for user " + userId + ": " + cart.toJson());
        return cart;
    }


    public void addToCart(int userId, int productId, int quantity) {
        Product product = productService.getProduct(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        Cart cart = getCart(userId);
        CartItem item = new CartItem(
                productId,
                quantity,
                product.getPrice(),
                product.getName(),
                product.getImageUrl()
        );
        cart.addItem(item);
        dataManager.saveCart(cart);
    }

    public void removeFromCart(int userId, int productId) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(item -> item.getProductId() == productId);
        dataManager.saveCart(cart);
    }

    public void clearCart(int userId) {
        Cart cart = getCart(userId);
        if (cart != null) {
            cart.clearItems();
            dataManager.saveCart(cart);
        }
    }
}