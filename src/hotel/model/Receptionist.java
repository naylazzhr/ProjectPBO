package hotel.model;

public class Receptionist extends User {
    private String shiftWork;

    // Constructor menggunakan super() dari User
    public Receptionist(String userId, String username, String password, String shiftWork) {
        super(userId, username, password, "RECEPTIONIST");
        this.shiftWork = shiftWork;
    }

    public String getShiftWork() {
        return shiftWork;
    }

    public void setShiftWork(String shiftWork) {
        this.shiftWork = shiftWork;
    }
}