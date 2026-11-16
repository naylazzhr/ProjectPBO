package hotel.model;

/**
 * Kamar tipe Standard. Konsep OOP: inheritance + overriding.
 */
public class StandardRoom extends Room {

    public StandardRoom(String nomor, int lantai, double hargaDasar, RoomStatus status) {
        super(nomor, lantai, hargaDasar, status);
    }

    @Override
    public String getTipe() { return "STANDARD"; }

    @Override
    public double hitungHargaPerMalam() {
        return getHargaDasar();            // tanpa tambahan
    }

    @Override
    public String getFasilitas() {
        return "AC, TV, kamar mandi dalam";
    }
}