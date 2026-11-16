package hotel.util;

/**
 * Exception induk untuk semua kesalahan di aplikasi hotel.
 * Konsep OOP: custom exception + inheritance.
 * Dibuat unchecked (RuntimeException) supaya method tidak perlu
 * menulis "throws" di mana-mana; tetap bisa ditangkap dengan try-catch.
 */
public class HotelException extends RuntimeException {

    public HotelException(String pesan) {
        super(pesan);
    }

    public HotelException(String pesan, Throwable penyebab) {
        super(pesan, penyebab);
    }
}