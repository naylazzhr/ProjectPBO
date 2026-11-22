package hotel.service;

import hotel.model.Guest;
import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.repository.GuestRepository;
import hotel.repository.ReservationRepository;
import hotel.view.GuestPanel;
import hotel.view.ReservationPanel;
import hotel.model.DeluxeRoom;
import hotel.model.RoomStatus;
import hotel.model.StandardRoom;
import hotel.model.SuiteRoom;
import hotel.repository.RoomRepository;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ReservationServiceIntegrationTest {
    private ReservationServiceIntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        Path dataDirectory = Files.createTempDirectory("hotel-reservation-test-");
        try {
            runScenarios(dataDirectory);
            System.out.println("Semua skenario reservasi lulus.");
        } finally {
            deleteDirectory(dataDirectory);
        }
    }

    private static void runScenarios(Path dataDirectory) throws Exception {
        GuestRepository guests = new GuestRepository(dataDirectory.resolve("guests.csv"));
        ReservationRepository reservations = new ReservationRepository(dataDirectory.resolve("reservations.csv"));
        RoomRepository rooms = new RoomRepository(dataDirectory.resolve("rooms.csv"));
        guests.load();
        reservations.load();
        rooms.muat();

        guests.add(new Guest(
                "G-TEST-1",
                "Tamu Pengujian",
                "1234567890123456",
                "+62 812-3456-7890",
                "uji@example.com",
                "Regular Guest"
        ));
        rooms.tambah(new StandardRoom("101", 1, 300000, RoomStatus.AVAILABLE));
        rooms.tambah(new DeluxeRoom("201", 2, 300000, RoomStatus.AVAILABLE));
        rooms.tambah(new SuiteRoom("301", 3, 300000, RoomStatus.MAINTENANCE));

        ReservationService service = new ReservationService(
                guests,
                reservations,
                new RoomRepositoryGateway(rooms)
        );
        LocalDate checkIn = LocalDate.of(2026, 12, 1);
        LocalDate firstCheckOut = checkIn.plusDays(2);
        LocalDate secondCheckOut = checkIn.plusDays(4);

        assertEquals(
                List.of("101", "201"),
                service.findAvailableRooms(checkIn, firstCheckOut),
                "Daftar kamar tersedia mengecualikan kamar perawatan"
        );

        Reservation first = service.createReservation("G-TEST-1", "101", checkIn, firstCheckOut);
        assertEquals(ReservationStatus.BOOKED, first.getStatus(), "Reservasi baru berstatus Booked");
        assertEquals(RoomStatus.RESERVED, rooms.cari("101").getStatus(), "Kamar berubah menjadi Reserved");

        expectFailure(
                IllegalStateException.class,
                () -> service.createReservation("G-TEST-1", "101", checkIn.plusDays(1), secondCheckOut),
                "Reservasi yang tanggalnya bentrok ditolak"
        );

        Reservation adjacent = service.createReservation(
                "G-TEST-1",
                "101",
                firstCheckOut,
                secondCheckOut
        );
        Reservation moved = service.updateReservation(
                adjacent.getId(),
                "201",
                firstCheckOut,
                secondCheckOut
        );
        assertEquals("201", moved.getRoomNumber(), "Reservasi sebelum check-in dapat dipindah kamar");
        assertEquals(RoomStatus.RESERVED, rooms.cari("201").getStatus(), "Kamar tujuan berubah menjadi Reserved");

        Reservation cancelled = service.cancelReservation(moved.getId());
        assertEquals(ReservationStatus.CANCELLED, cancelled.getStatus(), "Reservasi dapat dibatalkan");
        assertEquals(RoomStatus.AVAILABLE, rooms.cari("201").getStatus(), "Pembatalan mengembalikan status kamar");

        Reservation checkedIn = service.checkIn(first.getId());
        assertEquals(ReservationStatus.CHECKED_IN, checkedIn.getStatus(), "Check-in mengubah status reservasi");
        assertEquals(600000L, checkedIn.getTotalCost().longValueExact(), "Total dihitung dari tarif dikali malam");
        assertEquals(RoomStatus.OCCUPIED, rooms.cari("101").getStatus(), "Check-in mengubah status kamar");
        assertEquals(
                false,
                service.isRoomAvailable("101", checkIn.plusDays(1), firstCheckOut),
                "Kamar yang sedang dipesan tidak tersedia pada tanggal bentrok"
        );

        Reservation checkedOut = service.checkOut(first.getId());
        assertEquals(ReservationStatus.CHECKED_OUT, checkedOut.getStatus(), "Check-out mengubah status reservasi");
        assertEquals(RoomStatus.AVAILABLE, rooms.cari("101").getStatus(), "Check-out mengembalikan status kamar");
        assertEquals(
                false,
                service.isRoomAvailable("301", checkIn, firstCheckOut),
                "Kamar perawatan tidak bisa dipesan"
        );

        GuestRepository reloadedGuests = new GuestRepository(dataDirectory.resolve("guests.csv"));
        ReservationRepository reloadedReservations = new ReservationRepository(dataDirectory.resolve("reservations.csv"));
        RoomRepository reloadedRooms = new RoomRepository(dataDirectory.resolve("rooms.csv"));
        reloadedGuests.load();
        reloadedReservations.load();
        reloadedRooms.muat();
        assertEquals(1, reloadedGuests.findAll().size(), "Data tamu tersimpan di CSV");
        assertEquals(
                ReservationStatus.CHECKED_OUT,
                reloadedReservations.findById(first.getId()).orElseThrow().getStatus(),
                "Status reservasi tersimpan di CSV"
        );
        assertEquals(
                ReservationStatus.CANCELLED,
                reloadedReservations.findById(moved.getId()).orElseThrow().getStatus(),
                "Status pembatalan tersimpan di CSV"
        );
        assertEquals(RoomStatus.AVAILABLE, reloadedRooms.cari("101").getStatus(), "Status kamar tersimpan di CSV");
        SwingUtilities.invokeAndWait(() -> {
            new GuestPanel(reloadedGuests, service);
            new ReservationPanel(service);
        });
        System.out.println("PASS: cek ketersediaan, bentrok tanggal, ubah/batal, check-in/out, tarif, CSV.");
        System.out.println("PASS: panel tamu dan reservasi dapat dibangun dengan data hasil uji.");
    }

    private static void expectFailure(
            Class<? extends Exception> expectedType,
            ThrowingAction action,
            String scenario
    ) throws Exception {
        try {
            action.run();
        } catch (Exception ex) {
            if (expectedType.isInstance(ex)) {
                System.out.println("PASS: " + scenario);
                return;
            }
            throw new AssertionError(scenario + " menghasilkan exception yang tidak sesuai.", ex);
        }
        throw new AssertionError(scenario + " seharusnya gagal.");
    }

    private static void assertEquals(Object expected, Object actual, String scenario) {
        if (!expected.equals(actual)) {
            throw new AssertionError(scenario + ": diharapkan " + expected + ", diperoleh " + actual + ".");
        }
    }

    private static void deleteDirectory(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.deleteIfExists(path);
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }
}
