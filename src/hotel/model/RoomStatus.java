package hotel.model;

/**
 * Status sebuah kamar.
 */
public enum RoomStatus {
    AVAILABLE,   // kosong, bisa dipesan
    RESERVED,    // sudah dipesan, belum check-in
    OCCUPIED     // sedang dihuni (sudah check-in)
}