package hotel.model;

import hotel.model.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SEMENTARA: hanya untuk mengetes hierarki Room.
 * Jangan dipakai sebagai MainApp; hapus kalau sudah tidak perlu.
 */
public class RoomTest {
    public static void main(String[] args) {
        List<Room> daftar = new ArrayList<>();
        daftar.add(new StandardRoom("101", 1, 300000, RoomStatus.AVAILABLE));
        daftar.add(new DeluxeRoom("201", 2, 300000, RoomStatus.RESERVED));
        daftar.add(new SuiteRoom("301", 3, 300000, RoomStatus.OCCUPIED));

        // Polymorphism: tipe variabelnya Room, tapi method yang jalan milik subclass
        for (Room r : daftar) {
            System.out.println(r);
            System.out.println("   Fasilitas: " + r.getFasilitas());
        }
    }
}