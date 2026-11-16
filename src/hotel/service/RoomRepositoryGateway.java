package com.grandluxury.service;

import com.grandluxury.service.ReservationService.RoomGateway;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.repository.RoomRepository;
import hotel.util.DataNotFoundException;
import hotel.util.HotelException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class RoomRepositoryGateway implements RoomGateway {
    private final RoomRepository roomRepository;

    public RoomRepositoryGateway(RoomRepository roomRepository) {
        this.roomRepository = Objects.requireNonNull(roomRepository);
    }

    @Override
    public boolean isOperational(String roomNumber) {
        try {
            return roomRepository.cari(roomNumber).getStatus() != RoomStatus.MAINTENANCE;
        } catch (DataNotFoundException ex) {
            return false;
        }
    }

    @Override
    public BigDecimal getNightlyRate(String roomNumber) {
        return BigDecimal.valueOf(room(roomNumber).hitungHargaPerMalam());
    }

    @Override
    public void markReserved(String roomNumber) throws IOException {
        Room room = room(roomNumber);
        if (room.getStatus() == RoomStatus.AVAILABLE) {
            room.pesan();
            saveRoomState();
        }
    }

    @Override
    public void markOccupied(String roomNumber) throws IOException {
        Room room = room(roomNumber);
        if (room.getStatus() == RoomStatus.AVAILABLE) {
            room.pesan();
        }
        if (room.getStatus() == RoomStatus.RESERVED) {
            room.checkIn();
            saveRoomState();
        } else if (room.getStatus() == RoomStatus.MAINTENANCE) {
            throw new IllegalStateException("Kamar " + roomNumber + " sedang dalam perawatan.");
        }
    }

    @Override
    public void markAvailable(String roomNumber) throws IOException {
        Room room = room(roomNumber);
        if (room.getStatus() == RoomStatus.RESERVED) {
            room.batalkanPesanan();
            saveRoomState();
        } else if (room.getStatus() == RoomStatus.OCCUPIED) {
            room.checkOut();
            saveRoomState();
        }
    }

    @Override
    public List<String> getRoomNumbers() {
        return roomRepository.tampil().stream()
                .map(Room::getNomor)
                .sorted()
                .collect(Collectors.toList());
    }

    private Room room(String roomNumber) {
        return roomRepository.cari(roomNumber);
    }

    private void saveRoomState() throws IOException {
        try {
            roomRepository.simpan();
        } catch (HotelException ex) {
            if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            }
            throw ex;
        }
    }
}
