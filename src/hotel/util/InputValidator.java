package hotel.util;

public class InputValidator {

    // Validasi apakah string tidak kosong atau null
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    // Validasi panjang minimal password
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Validasi nominal uang/pembayaran harus bernilai positif (> 0)
    public static boolean isValidAmount(double amount) {
        return amount > 0;
    }
}