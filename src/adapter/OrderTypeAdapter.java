package adapter;

import com.google.gson.*;
import model.Order;
import model.CartItem;
import model.ShippingAddress;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class OrderTypeAdapter implements JsonSerializer<Order>, JsonDeserializer<Order> {
    @Override
    public JsonElement serialize(Order order, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("id", order.getId());
        json.addProperty("userId", order.getUserId());
        json.addProperty("total", order.getTotal());
        json.addProperty("status", order.getStatus());
        json.addProperty("orderDate", order.getOrderDate());

        JsonArray items = new JsonArray();
        for (CartItem item : order.getItems()) {
            items.add(context.serialize(item));
        }
        json.add("items", items);

        if (order.getShippingAddress() != null) {
            json.add("shippingAddress", context.serialize(order.getShippingAddress()));
        }

        return json;
    }

    @Override
    public Order deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        int id = jsonObject.get("id").getAsInt();
        int userId = jsonObject.get("userId").getAsInt();

        Order order = new Order(id, userId);

        if (jsonObject.has("total")) {
            order.setTotal(jsonObject.get("total").getAsDouble());
        }
        if (jsonObject.has("status")) {
            order.setStatus(jsonObject.get("status").getAsString());
        }
        if (jsonObject.has("orderDate")) {
            order.setOrderDate(jsonObject.get("orderDate").getAsString());
        }

        if (jsonObject.has("items")) {
            JsonArray items = jsonObject.getAsJsonArray("items");
            for (JsonElement item : items) {
                CartItem cartItem = context.deserialize(item, CartItem.class);
                order.addItem(cartItem);
            }
        }

        if (jsonObject.has("shippingAddress")) {
            ShippingAddress address = context.deserialize(
                    jsonObject.get("shippingAddress"),
                    ShippingAddress.class
            );
            order.setShippingAddress(address);
        }

        return order;
    }
}