package model;

public class Institution {
    private final int institutionId;
    private final int adminId;
    private final String name;
    private final String description;
    private final String joinCode;

    public Institution(int institutionId, int adminId, String name,
                       String description, String joinCode) {
        this.institutionId = institutionId;
        this.adminId = adminId;
        this.name = name;
        this.description = description;
        this.joinCode = joinCode;
    }

    public int getInstitutionId() { return institutionId; }
    public int getAdminId() { return adminId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getJoinCode() { return joinCode; }
}