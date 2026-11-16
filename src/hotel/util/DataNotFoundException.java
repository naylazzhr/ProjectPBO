package hotel.util;

/**
 * Dilempar saat data yang dicari tidak ditemukan
 * (misalnya kamar dengan nomor tertentu tidak ada).
 */
public class DataNotFoundException extends HotelException {

    public DataNotFoundException(String pesan) {
        super(pesan);
    }
}