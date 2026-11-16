package hotel.util;

/**
 * Dilempar saat input tidak valid
 * (misalnya harga <= 0, nomor kamar kosong, atau ID ganda).
 */
public class InvalidInputException extends HotelException {

    public InvalidInputException(String pesan) {
        super(pesan);
    }
}