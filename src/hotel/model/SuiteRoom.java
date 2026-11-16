package hotel.model;

/**
 * Kamar tipe Suite. Harga = harga dasar x faktor tarif.
 */
public class SuiteRoom extends Room {

    private static final double FAKTOR_TARIF = 1.75;

    public SuiteRoom(String nomor, int lantai, double hargaDasar, RoomStatus status) {
        super(nomor, lantai, hargaDasar, status);
    }

    @Override
    public String getTipe() { return "SUITE"; }

    @Override
    public double hitungHargaPerMalam() {
        return getHargaDasar() * FAKTOR_TARIF;
    }

    @Override
    public String getFasilitas() {
        return "AC, Smart TV, ruang tamu, bathtub, minibar, sarapan, layanan kamar";
    }
}