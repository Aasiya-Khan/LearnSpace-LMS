package model;

public class InstitutionAdmin extends User {
    public InstitutionAdmin(int id, String name, String email, String hash) {
        super(id, name, email, hash);
    }
    @Override
    public String getRole() { return "INSTITUTION_ADMIN"; }
}