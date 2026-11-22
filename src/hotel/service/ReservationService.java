package hotel.service;

import hotel.model.Guest;
import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.repository.GuestRepository;
import hotel.repository.ReservationRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class ReservationService {
    public interface RoomGateway {
        List<String> getRoomNumbers();

        boolean isOperational(String roomNumber) throws IOException;

        BigDecimal getNightlyRate(String roomNumber) throws IOException;

        void markReserved(String roomNumber) throws IOException;

        void markOccupied(String roomNumber) throws IOException;

        void markAvailable(String roomNumber) throws IOException;
    }

    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final RoomGateway roomGateway;

    public ReservationService(
            GuestRepository guestRepository,
            ReservationRepository reservationRepository,
            RoomGateway roomGateway
    ) {
        this.guestRepository = Objects.requireNonNull(guestRepository);
        this.reservationRepository = Objects.requireNonNull(reservationRepository);
        this.roomGateway = Objects.requireNonNull(roomGateway);
    }

    public Reservation createReservation(
            String guestId,
            String roomNumber,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) throws IOException {
        guestId = required(guestId, "ID tamu");
        roomNumber = required(roomNumber, "Nomor kamar");
        if (guestRepository.findById(guestId).isEmpty()) {
            throw new NoSuchElementException("Tamu tidak ditemukan: " + guestId);
        }
        Reservation reservation = new Reservation(
                newReservationId(),
                guestId,
                roomNumber,
                checkInDate,
                checkOutDate
        );
        requireRoomAvailable(roomNumber, checkInDate, checkOutDate, null);

        roomGateway.markReserved(roomNumber);
        try {
            reservationRepository.add(reservation);
        } catch (IOException | RuntimeException ex) {
            restoreRoomStatus(roomNumber, ex);
            throw ex;
        }
        return reservation;
    }

    public Reservation updateReservation(
            String reservationId,
            String newRoomNumber,
            LocalDate newCheckInDate,
            LocalDate newCheckOutDate
    ) throws IOException {
        reservationId = required(reservationId, "ID reservasi");
        newRoomNumber = required(newRoomNumber, "Nomor kamar");
        Reservation current = requireReservation(reservationId);
        Reservation updated = current.reschedule(newRoomNumber, newCheckInDate, newCheckOutDate);
        requireRoomAvailable(newRoomNumber, newCheckInDate, newCheckOutDate, reservationId);

        boolean roomChanged = !current.getRoomNumber().equals(updated.getRoomNumber());
        if (roomChanged) {
            roomGateway.markReserved(updated.getRoomNumber());
        }
        try {
            reservationRepository.update(updated);
        } catch (IOException | RuntimeException ex) {
            if (roomChanged) {
                restoreRoomStatus(updated.getRoomNumber(), ex);
            }
            throw ex;
        }
        if (roomChanged) {
            synchronizeRoomStatus(current.getRoomNumber());
        }
        return updated;
    }

    public Reservation cancelReservation(String reservationId) throws IOException {
        Reservation current = requireReservation(reservationId);
        Reservation cancelled = current.cancel();
        reservationRepository.update(cancelled);
        synchronizeRoomStatus(current.getRoomNumber());
        return cancelled;
    }

    public Reservation checkIn(String reservationId) throws IOException {
        Reservation current = requireReservation(reservationId);
        BigDecimal nightlyRate = roomGateway.getNightlyRate(current.getRoomNumber());
        Reservation checkedIn = current.checkIn(nightlyRate);
        roomGateway.markOccupied(current.getRoomNumber());
        try {
            reservationRepository.update(checkedIn);
        } catch (IOException | RuntimeException ex) {
            restoreRoomStatus(current.getRoomNumber(), ex);
            throw ex;
        }
        return checkedIn;
    }

    public Reservation checkOut(String reservationId) throws IOException {
        Reservation current = requireReservation(reservationId);
        Reservation checkedOut = current.checkOut();
        reservationRepository.update(checkedOut);
        synchronizeRoomStatus(current.getRoomNumber());
        return checkedOut;
    }

    public boolean isRoomAvailable(String roomNumber, LocalDate checkInDate, LocalDate checkOutDate) throws IOException {
        roomNumber = required(roomNumber, "Nomor kamar");
        validateStayDates(checkInDate, checkOutDate);
        return roomGateway.isOperational(roomNumber)
                && reservationRepository.findOverlapping(roomNumber, checkInDate, checkOutDate).isEmpty();
    }

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    public List<String> findAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) throws IOException {
        validateStayDates(checkInDate, checkOutDate);
        List<String> available = new java.util.ArrayList<>();
        for (String roomNumber : roomGateway.getRoomNumbers()) {
            if (isRoomAvailable(roomNumber, checkInDate, checkOutDate)) {
                available.add(roomNumber);
            }
        }
        return available;
    }

    public List<Reservation> findReservationsByGuest(String guestId) {
        return reservationRepository.findByGuestId(guestId);
    }

    public Guest findGuest(String guestId) {
        return guestRepository.findById(guestId)
                .orElseThrow(() -> new NoSuchElementException("Tamu tidak ditemukan: " + guestId));
    }

    private void requireRoomAvailable(
            String roomNumber,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String excludingReservationId
    ) throws IOException {
        roomNumber = required(roomNumber, "Nomor kamar");
        validateStayDates(checkInDate, checkOutDate);
        if (!roomGateway.isOperational(roomNumber)) {
            throw new IllegalStateException("Kamar " + roomNumber + " tidak ditemukan atau sedang tidak beroperasi.");
        }
        for (Reservation existing : reservationRepository.findOverlapping(roomNumber, checkInDate, checkOutDate)) {
            if (!existing.getId().equals(excludingReservationId)) {
                throw new IllegalStateException("Kamar " + roomNumber + " sudah memiliki reservasi pada rentang tanggal tersebut.");
            }
        }
    }

    private Reservation requireReservation(String reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Reservasi tidak ditemukan: " + reservationId));
    }

    private void synchronizeRoomStatus(String roomNumber) throws IOException {
        boolean occupied = false;
        boolean reserved = false;
        for (Reservation reservation : reservationRepository.findByRoomNumber(roomNumber)) {
            if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
                occupied = true;
                break;
            }
            if (reservation.getStatus() == ReservationStatus.BOOKED) {
                reserved = true;
            }
        }
        if (occupied) {
            roomGateway.markOccupied(roomNumber);
        } else if (reserved) {
            roomGateway.markAvailable(roomNumber);
            roomGateway.markReserved(roomNumber);
        } else {
            roomGateway.markAvailable(roomNumber);
        }
    }

    private void restoreRoomStatus(String roomNumber, Exception originalError) {
        try {
            synchronizeRoomStatus(roomNumber);
        } catch (IOException | RuntimeException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }

    private static String newReservationId() {
        return "RSV-" + LocalDate.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String required(String value, String label) {
        Objects.requireNonNull(value, label + " tidak boleh null.");
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(label + " wajib diisi.");
        }
        return normalized;
    }

    private static void validateStayDates(LocalDate checkInDate, LocalDate checkOutDate) {
        Objects.requireNonNull(checkInDate, "Tanggal check-in tidak boleh null.");
        Objects.requireNonNull(checkOutDate, "Tanggal check-out tidak boleh null.");
        if (!checkInDate.isBefore(checkOutDate)) {
            throw new IllegalArgumentException("Tanggal check-out harus setelah tanggal check-in.");
        }
    }
}