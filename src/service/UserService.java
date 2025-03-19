package service;

import model.User;
import utils.JsonDataManager;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private static UserService instance;
    private final JsonDataManager dataManager;
    private static final String USERS_FILE = "users.json";
    private static final Type USER_LIST_TYPE = new TypeToken<ArrayList<User>>(){}.getType();

    private UserService() {
        this.dataManager = JsonDataManager.getInstance();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public Collection<User> getAllUsers() {
        String json = dataManager.readFile(USERS_FILE);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        return dataManager.getGson().fromJson(json, USER_LIST_TYPE);
    }

    public User getUser(int id) {
        return getAllUsers().stream()
                .filter(user -> user.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public User getUserByEmail(String email) {
        String json = dataManager.readFile(USERS_FILE);
        if (json == null || json.isEmpty()) {
            return null;
        }
        Collection<User> users = dataManager.getGson().fromJson(json, USER_LIST_TYPE);

        return users.stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst()
                .orElse(null);
    }

    public void saveUser(User user) {
        Collection<User> users = getAllUsers();
        List<User> userList = new ArrayList<>(users);

        // Update existing user or add new one
        boolean found = false;
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId() == user.getId()) {
                userList.set(i, user);
                found = true;
                break;
            }
        }

        if (!found) {
            userList.add(user);
        }

        String json = dataManager.getGson().toJson(userList);
        dataManager.writeFile(USERS_FILE, json);
    }

    public void deleteUser(int id) {
        Collection<User> users = getAllUsers();
        List<User> updatedUsers = users.stream()
                .filter(user -> user.getId() != id)
                .collect(Collectors.toList());
        String json = dataManager.getGson().toJson(updatedUsers);
        dataManager.writeFile(USERS_FILE, json);
    }

    public String getAllUsersWithRole(String role) {
        Collection<User> allUsers = dataManager.getAllUsers();
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> role.equals(user.getRole()))
                .collect(Collectors.toList());
        return dataManager.getGson().toJson(filteredUsers);
    }

    public boolean authenticateUser(String email, String password) {
        User user = getUserByEmail(email);
        return user != null && user.getPassword().equals(password);
    }

    public boolean isAdmin(String email) {
        User user = getUserByEmail(email);
        return user != null && "admin".equals(user.getRole());
    }

    public User signIn(String email, String password) {
        try {
            Collection<User> users = getAllUsers();
            System.out.println("All users: " + dataManager.getGson().toJson(users));

            User user = getUserByEmail(email);
            if (user == null) {
                System.out.println("Sign-in failed: User not found - " + email);
                throw new RuntimeException("User not found");
            }

            System.out.println("Attempting sign-in for user: " + email);
            System.out.println("Stored password: " + user.getPassword());
            System.out.println("Provided password: " + password);

            if (!user.getPassword().trim().equals(password.trim())) {
                System.out.println("Sign-in failed: Invalid password");
                throw new RuntimeException("Invalid credentials");
            }

            System.out.println("Sign-in successful for user: " + email);
            return user;
        } catch (Exception e) {
            System.out.println("Sign-in error: " + e.getMessage());
            throw new RuntimeException("Invalid credentials");
        }
    }

    public User createAccount(String email, String password, String role) {
        // Check if email already exists
        if (dataManager.getUsers().values().stream()
                .anyMatch(user -> user.getEmail().equals(email))) {
            throw new RuntimeException("Email already exists");
        }

        int newId = generateNewUserId();
        User newUser = new User(newId, email, password, role);
        dataManager.saveUser(newUser);
        return newUser;
    }

    public void initiatePasswordReset(String email) {
        User user = dataManager.getUsers().values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Email not found"));
        System.out.println("Password reset link sent to: " + email);
    }

    public void changePassword(int userId, String newPassword) {
        User user = dataManager.getUsers().get(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.setPassword(newPassword);
        dataManager.saveUser(user);
    }

    public boolean resetPassword(String email, String newPassword) {
        User user = getUserByEmail(email);
        if (user == null) {
            return false;
        }

        user.setPassword(newPassword);
        saveUser(user);
        return true;
    }

    private int generateNewUserId() {
        return dataManager.getUsers().values().stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0) + 1;
    }
}