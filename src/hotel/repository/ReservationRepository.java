package hotel.repository;

import hotel.model.Reservation;
import hotel.model.ReservationStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public class ReservationRepository implements Repository<Reservation, String> {
    private final Path file;
    private final Map<String, Reservation> reservations = new LinkedHashMap<>();

    public ReservationRepository() {
        this(Paths.get("data", "reservations.csv"));
    }

    public ReservationRepository(Path file) {
        this.file = Objects.requireNonNull(file);
    }

    @Override
    public void add(Reservation reservation) throws IOException {
        if (reservations.containsKey(reservation.getId())) {
            throw new IllegalArgumentException("ID reservasi sudah terdaftar: " + reservation.getId());
        }
        Map<String, Reservation> updated = new LinkedHashMap<>(reservations);
        updated.put(reservation.getId(), reservation);
        persist(updated);
        reservations.put(reservation.getId(), reservation);
    }

    @Override
    public void update(Reservation reservation) throws IOException {
        if (!reservations.containsKey(reservation.getId())) {
            throw new NoSuchElementException("Reservasi tidak ditemukan: " + reservation.getId());
        }
        Map<String, Reservation> updated = new LinkedHashMap<>(reservations);
        updated.put(reservation.getId(), reservation);
        persist(updated);
        reservations.put(reservation.getId(), reservation);
    }

    @Override
    public void delete(String id) throws IOException {
        if (!reservations.containsKey(id)) {
            throw new NoSuchElementException("Reservasi tidak ditemukan: " + id);
        }
        Map<String, Reservation> updated = new LinkedHashMap<>(reservations);
        updated.remove(id);
        persist(updated);
        reservations.remove(id);
    }

    @Override
    public Optional<Reservation> findById(String id) {
        return Optional.ofNullable(reservations.get(id));
    }

    public List<Reservation> findByGuestId(String guestId) {
        List<Reservation> matches = new ArrayList<>();
        for (Reservation reservation : reservations.values()) {
            if (reservation.getGuestId().equals(guestId)) {
                matches.add(reservation);
            }
        }
        return matches;
    }

    public List<Reservation> findByRoomNumber(String roomNumber) {
        List<Reservation> matches = new ArrayList<>();
        for (Reservation reservation : reservations.values()) {
            if (reservation.getRoomNumber().equals(roomNumber)) {
                matches.add(reservation);
            }
        }
        return matches;
    }

    public List<Reservation> findOverlapping(String roomNumber, LocalDate checkIn, LocalDate checkOut) {
        List<Reservation> matches = new ArrayList<>();
        for (Reservation reservation : reservations.values()) {
            boolean active = reservation.getStatus() == ReservationStatus.BOOKED
                    || reservation.getStatus() == ReservationStatus.CHECKED_IN;
            boolean overlaps = checkIn.isBefore(reservation.getCheckOutDate())
                    && reservation.getCheckInDate().isBefore(checkOut);
            if (active && reservation.getRoomNumber().equals(roomNumber) && overlaps) {
                matches.add(reservation);
            }
        }
        return matches;
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    @Override
    public void save() throws IOException {
        persist(reservations);
    }

    @Override
    public void load() throws IOException {
        Map<String, Reservation> loaded = new LinkedHashMap<>();
        for (List<String> row : CsvSupport.read(file, 7)) {
            Reservation reservation;
            try {
                reservation = new Reservation(
                        row.get(0),
                        row.get(1),
                        row.get(2),
                        LocalDate.parse(row.get(3)),
                        LocalDate.parse(row.get(4)),
                        ReservationStatus.valueOf(row.get(5)),
                        new BigDecimal(row.get(6))
                );
            } catch (IllegalArgumentException | java.time.DateTimeException ex) {
                throw new IOException("Data reservasi tidak valid di " + file + ".", ex);
            }
            if (loaded.putIfAbsent(reservation.getId(), reservation) != null) {
                throw new IOException("ID reservasi duplikat di " + file + ": " + reservation.getId());
            }
        }
        reservations.clear();
        reservations.putAll(loaded);
    }

    private void persist(Map<String, Reservation> source) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        for (Reservation reservation : source.values()) {
            rows.add(List.of(
                    reservation.getId(),
                    reservation.getGuestId(),
                    reservation.getRoomNumber(),
                    reservation.getCheckInDate().toString(),
                    reservation.getCheckOutDate().toString(),
                    reservation.getStatus().name(),
                    reservation.getTotalCost().toPlainString()
            ));
        }
        CsvSupport.write(file, rows);
    }
}