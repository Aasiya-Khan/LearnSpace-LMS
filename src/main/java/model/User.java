package model;

public abstract class User {
    private final int userId;
    private final String name;
    private final String email;
    private final String passwordHash;

    public User(int userId, String name, String email, String passwordHash) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }

    public abstract String getRole();
}