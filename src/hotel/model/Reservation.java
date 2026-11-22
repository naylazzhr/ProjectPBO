package hotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class Reservation {
    private final String id;
    private final String guestId;
    private final String roomNumber;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final ReservationStatus status;
    private final BigDecimal totalCost;

    public Reservation(String id, String guestId, String roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        this(id, guestId, roomNumber, checkInDate, checkOutDate, ReservationStatus.BOOKED, BigDecimal.ZERO);
    }

    public Reservation(
            String id,
            String guestId,
            String roomNumber,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            ReservationStatus status,
            BigDecimal totalCost
    ) {
        this.id = required(id, "ID reservasi");
        this.guestId = required(guestId, "ID tamu");
        this.roomNumber = required(roomNumber, "Nomor kamar");
        this.checkInDate = Objects.requireNonNull(checkInDate, "Tanggal check-in tidak boleh null.");
        this.checkOutDate = Objects.requireNonNull(checkOutDate, "Tanggal check-out tidak boleh null.");
        this.status = Objects.requireNonNull(status, "Status reservasi tidak boleh null.");
        this.totalCost = Objects.requireNonNull(totalCost, "Total biaya tidak boleh null.");

        if (!checkInDate.isBefore(checkOutDate)) {
            throw new IllegalArgumentException("Tanggal check-out harus setelah tanggal check-in.");
        }
        if (totalCost.signum() < 0) {
            throw new IllegalArgumentException("Total biaya tidak boleh negatif.");
        }
    }

    private static String required(String value, String label) {
        Objects.requireNonNull(value, label + " tidak boleh null.");
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.contains("\n") || normalized.contains("\r")) {
            throw new IllegalArgumentException(label + " wajib diisi dan tidak boleh memuat baris baru.");
        }
        return normalized;
    }

    public Reservation reschedule(String newRoomNumber, LocalDate newCheckInDate, LocalDate newCheckOutDate) {
        requireStatus(ReservationStatus.BOOKED, "Reservasi hanya dapat diubah sebelum check-in.");
        return new Reservation(id, guestId, newRoomNumber, newCheckInDate, newCheckOutDate, status, totalCost);
    }

    public Reservation checkIn(BigDecimal nightlyRate) {
        requireStatus(ReservationStatus.BOOKED, "Check-in hanya dapat dilakukan untuk reservasi berstatus Booked.");
        Objects.requireNonNull(nightlyRate, "Tarif kamar tidak boleh null.");
        if (nightlyRate.signum() < 0) {
            throw new IllegalArgumentException("Tarif kamar tidak boleh negatif.");
        }
        BigDecimal total = nightlyRate.multiply(BigDecimal.valueOf(getNightCount()));
        return new Reservation(id, guestId, roomNumber, checkInDate, checkOutDate, ReservationStatus.CHECKED_IN, total);
    }

    public Reservation checkOut() {
        requireStatus(ReservationStatus.CHECKED_IN, "Check-out hanya dapat dilakukan setelah check-in.");
        return new Reservation(id, guestId, roomNumber, checkInDate, checkOutDate, ReservationStatus.CHECKED_OUT, totalCost);
    }

    public Reservation cancel() {
        requireStatus(ReservationStatus.BOOKED, "Hanya reservasi yang belum check-in yang dapat dibatalkan.");
        return new Reservation(id, guestId, roomNumber, checkInDate, checkOutDate, ReservationStatus.CANCELLED, totalCost);
    }

    private void requireStatus(ReservationStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    public long getNightCount() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public String getId() {
        return id;
    }

    public String getGuestId() {
        return guestId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }
}