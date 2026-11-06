package model;

public class Instructor extends User {
    public Instructor(int id, String name, String email, String hash) {
        super(id, name, email, hash);
    }
    @Override
    public String getRole() { return "INSTRUCTOR"; }
}