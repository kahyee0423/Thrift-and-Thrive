package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import model.CartItem;

import java.io.IOException;

public class CartItemTypeAdapter extends TypeAdapter<CartItem> {
    @Override
    public void write(JsonWriter out, CartItem item) throws IOException {
        out.beginObject();
        out.name("productId").value(item.getProductId());
        out.name("quantity").value(item.getQuantity());
        out.name("price").value(item.getPrice());
        out.name("productName").value(item.getProductName());
        out.name("imageUrl").value(item.getImageUrl());
        out.endObject();
    }

    @Override
    public CartItem read(JsonReader in) throws IOException {
        int productId = 0;
        int quantity = 0;
        double price = 0.0;
        String productName = "";
        String imageUrl = "";

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "productId":
                    productId = in.nextInt();
                    break;
                case "quantity":
                    quantity = in.nextInt();
                    break;
                case "price":
                    price = in.nextDouble();
                    break;
                case "productName":
                    productName = in.nextString();
                    break;
                case "imageUrl":
                    imageUrl = in.nextString();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return new CartItem(productId, quantity, price, productName, imageUrl);
    }
}
