package model;

public class Student extends User {
    public Student(int id, String name, String email, String hash) {
        super(id, name, email, hash);
    }
    @Override
    public String getRole() { return "STUDENT"; }
}