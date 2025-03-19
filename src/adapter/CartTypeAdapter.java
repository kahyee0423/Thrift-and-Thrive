package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import model.Cart;
import model.CartItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CartTypeAdapter extends TypeAdapter<Cart> {
    @Override
    public void write(JsonWriter out, Cart cart) throws IOException {
        out.beginObject();
        out.name("userId").value(cart.getUserId());
        out.name("items");
        out.beginArray();
        for (CartItem item : cart.getItems()) {
            new CartItemTypeAdapter().write(out, item);
        }
        out.endArray();
        out.name("total").value(cart.getTotal());
        out.endObject();
    }

    @Override
    public Cart read(JsonReader in) throws IOException {
        Cart cart = null;
        List<CartItem> items = new ArrayList<>();

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "userId":
                    cart = new Cart(in.nextInt());
                    break;
                case "items":
                    in.beginArray();
                    while (in.hasNext()) {
                        items.add(new CartItemTypeAdapter().read(in));
                    }
                    in.endArray();
                    break;
                case "total":
                    double total = in.nextDouble();
                    if (cart != null) {
                        cart.setTotal(total);
                    }
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        if (cart != null) {
            cart.setItems(items);
        }
        return cart;
    }
}
