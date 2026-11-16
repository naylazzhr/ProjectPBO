package hotel.model;

/**
 * Kamar tipe Deluxe. Harga = harga dasar x faktor tarif.
 */
public class DeluxeRoom extends Room {

    private static final double FAKTOR_TARIF = 1.30;

    public DeluxeRoom(String nomor, int lantai, double hargaDasar, RoomStatus status) {
        super(nomor, lantai, hargaDasar, status);
    }

    @Override
    public String getTipe() { return "DELUXE"; }

    @Override
    public double hitungHargaPerMalam() {
        return getHargaDasar() * FAKTOR_TARIF;
    }

    @Override
    public String getFasilitas() {
        return "AC, Smart TV, kamar mandi dalam, minibar, sarapan";
    }
}