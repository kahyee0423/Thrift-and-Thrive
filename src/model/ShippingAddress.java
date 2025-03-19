package model;

public class ShippingAddress {
    private final String fullName;
    private final String email;
    private final String phone;
    private final String address;
    private final String city;
    private final String state;
    private final String postalCode;

    public ShippingAddress(String fullName, String email, String phone,
                           String address, String city, String state, String postalCode) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
    }

    public String toJson() {
        return String.format(
                "{\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"," +
                        "\"address\":\"%s\",\"city\":\"%s\",\"state\":\"%s\",\"postalCode\":\"%s\"}",
                fullName, email, phone, address, city, state, postalCode
        );
    }
}