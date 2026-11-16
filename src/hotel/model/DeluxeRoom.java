package hotel.model;

/**
 * Kamar tipe Deluxe. Harga = harga dasar x faktor tarif.
 */
public class DeluxeRoom extends Room {

    // TODO: sepakati persentasenya dengan tim
    private static final double FAKTOR_TARIF = 1.25;

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