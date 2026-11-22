package hotel.model;

public class Admin extends User {
    private String adminAccessLevel;

    // Constructor menggunakan super() dari User
    public Admin(String userId, String username, String password, String adminAccessLevel) {
        super(userId, username, password, "ADMIN");
        this.adminAccessLevel = adminAccessLevel;
    }

    public String getAdminAccessLevel() {
        return adminAccessLevel;
    }

    public void setAdminAccessLevel(String adminAccessLevel) {
        this.adminAccessLevel = adminAccessLevel;
    }
}