package hotel.model;

import hotel.util.InvalidInputException;
import hotel.util.RoomNotAvailableException;

/**
 * Abstract class induk untuk semua tipe kamar.
 *
 * Konsep OOP:
 *  - Abstraction  : method abstract yang wajib di-override subclass
 *  - Encapsulation: semua atribut private; perubahan data hanya lewat
 *                   method yang memvalidasi (bukan diubah bebas dari luar)
 */
public abstract class Room {

    // ---------- Atribut (private = tidak bisa diakses langsung dari luar) ----------
    private final String nomor;      // final: identitas kamar tidak boleh berubah
    private final int lantai;        // final: lantai tidak berubah setelah dibuat
    private double hargaDasar;
    private RoomStatus status;

    // ---------- Constructor (validasi semua data sejak awal) ----------
    public Room(String nomor, int lantai, double hargaDasar, RoomStatus status) {
        if (nomor == null || nomor.trim().isEmpty()) {
            throw new InvalidInputException("Nomor kamar tidak boleh kosong");
        }
        if (lantai < 1) {
            throw new InvalidInputException("Lantai minimal 1");
        }
        if (status == null) {
            throw new InvalidInputException("Status kamar tidak boleh kosong");
        }
        this.nomor = nomor.trim();
        this.lantai = lantai;
        setHargaDasar(hargaDasar);   // lewat setter supaya aturan validasinya satu tempat
        this.status = status;
    }

    // ---------- Getter (hanya baca) ----------
    public String getNomor() { return nomor; }

    public int getLantai() { return lantai; }

    public double getHargaDasar() { return hargaDasar; }

    public RoomStatus getStatus() { return status; }

    // ---------- Setter dengan validasi ----------
    public void setHargaDasar(double hargaDasar) {
        if (hargaDasar <= 0) {
            throw new InvalidInputException("Harga dasar harus lebih dari 0");
        }
        this.hargaDasar = hargaDasar;
    }

    // ---------- Pengaturan status (tidak ada setStatus bebas) ----------
    // Status hanya boleh berubah lewat alur yang sah:
    // AVAILABLE -> RESERVED -> OCCUPIED -> AVAILABLE

    /** Memesan kamar: AVAILABLE -> RESERVED. */
    public void pesan() {
        if (status != RoomStatus.AVAILABLE) {
            throw new RoomNotAvailableException(
                    "Kamar " + nomor + " tidak bisa dipesan, status: " + status);
        }
        status = RoomStatus.RESERVED;
    }

    /** Membatalkan pesanan: RESERVED -> AVAILABLE. */
    public void batalkanPesanan() {
        if (status != RoomStatus.RESERVED) {
            throw new RoomNotAvailableException(
                    "Kamar " + nomor + " tidak sedang dipesan, status: " + status);
        }
        status = RoomStatus.AVAILABLE;
    }

    /** Check-in: RESERVED -> OCCUPIED. */
    public void checkIn() {
        if (status != RoomStatus.RESERVED) {
            throw new RoomNotAvailableException(
                    "Kamar " + nomor + " tidak bisa check-in, status: " + status);
        }
        status = RoomStatus.OCCUPIED;
    }

    /** Check-out: OCCUPIED -> AVAILABLE. */
    public void checkOut() {
        if (status != RoomStatus.OCCUPIED) {
            throw new RoomNotAvailableException(
                    "Kamar " + nomor + " tidak bisa check-out, status: " + status);
        }
        status = RoomStatus.AVAILABLE;
    }

    // ---------- Method abstract (wajib di-override subclass) ----------
    /** Tipe kamar, misalnya "STANDARD", "DELUXE", "SUITE". */
    public abstract String getTipe();

    /** Harga per malam; cara hitungnya berbeda tiap tipe (polymorphism). */
    public abstract double hitungHargaPerMalam();

    /** Daftar fasilitas kamar. */
    public abstract String getFasilitas();

    // ---------- Method umum ----------
    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return String.format("Kamar %s [%s] lantai %d - Rp%,.0f/malam - %s",
                nomor, getTipe(), lantai, hitungHargaPerMalam(), status);
    }
}