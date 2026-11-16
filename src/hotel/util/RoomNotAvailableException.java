package hotel.util;

/**
 * Dilempar saat kamar yang akan dipesan tidak berstatus AVAILABLE.
 */
public class RoomNotAvailableException extends HotelException {

    public RoomNotAvailableException(String pesan) {
        super(pesan);
    }
}