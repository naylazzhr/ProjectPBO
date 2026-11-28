package hotel.model;

import java.util.ArrayList;
import java.util.List;

public class RoomTest {
    public static void main(String[] args) {
        // --- Tes 1: polymorphism ---
        List<Room> daftar = new ArrayList<>();
        daftar.add(new StandardRoom("101", 1, 300000, RoomStatus.AVAILABLE));
        daftar.add(new DeluxeRoom("201", 2, 300000, RoomStatus.RESERVED));
        daftar.add(new SuiteRoom("301", 3, 300000, RoomStatus.OCCUPIED));

        for (Room kamar : daftar) {
            System.out.println(kamar);
            System.out.println("   Fasilitas: " + kamar.getFasilitas());
        }

        // --- Tes 2: alur status yang sah ---
        Room r = new StandardRoom("102", 1, 300000, RoomStatus.AVAILABLE);
        r.pesan();
        System.out.println("Setelah pesan    : " + r.getStatus());
        r.checkIn();
        System.out.println("Setelah check-in : " + r.getStatus());
        r.checkOut();
        System.out.println("Setelah check-out: " + r.getStatus());

        // --- Tes 3: alur yang salah harus ditolak ---
        try {
            r.checkIn();   // kamar kosong, tidak boleh check-in
        } catch (hotel.util.RoomNotAvailableException e) {
            System.out.println("Tertangkap: " + e.getMessage());
        }

        // --- Tes 4: data tidak valid harus ditolak ---
        try {
            new StandardRoom("103", 1, -5, RoomStatus.AVAILABLE);
        } catch (hotel.util.InvalidInputException e) {
            System.out.println("Tertangkap: " + e.getMessage());
        }
        try {
            new StandardRoom(" ", 1, 300000, RoomStatus.AVAILABLE);
        } catch (hotel.util.InvalidInputException e) {
            System.out.println("Tertangkap: " + e.getMessage());
        }
    }
}