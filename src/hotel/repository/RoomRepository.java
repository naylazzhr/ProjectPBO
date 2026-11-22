package hotel.repository;

import hotel.model.DeluxeRoom;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.model.StandardRoom;
import hotel.model.SuiteRoom;
import hotel.util.DataNotFoundException;
import hotel.util.HotelException;
import hotel.util.InvalidInputException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RoomRepository implements Repository<Room, String> {
    private final Path file;
    private final Map<String, Room> rooms = new LinkedHashMap<>();

    public RoomRepository() {
        this(Paths.get("data", "rooms.csv"));
    }

    public RoomRepository(Path file) {
        this.file = Objects.requireNonNull(file);
    }

    public void tambah(Room room) {
        Objects.requireNonNull(room, "Data kamar tidak boleh null.");
        validateCsvValue(room.getNomor());
        if (rooms.containsKey(room.getNomor())) {
            throw new InvalidInputException("Nomor kamar sudah terdaftar: " + room.getNomor());
        }
        Map<String, Room> updated = new LinkedHashMap<>(rooms);
        updated.put(room.getNomor(), room);
        write(updated);
        replaceContents(updated);
    }

    public void ubah(Room room) {
        Objects.requireNonNull(room, "Data kamar tidak boleh null.");
        validateCsvValue(room.getNomor());
        if (!rooms.containsKey(room.getNomor())) {
            throw new DataNotFoundException("Kamar tidak ditemukan: " + room.getNomor());
        }
        Map<String, Room> updated = new LinkedHashMap<>(rooms);
        updated.put(room.getNomor(), room);
        write(updated);
        replaceContents(updated);
    }

    public void hapus(String id) {
        if (!rooms.containsKey(id)) {
            throw new DataNotFoundException("Kamar tidak ditemukan: " + id);
        }
        Map<String, Room> updated = new LinkedHashMap<>(rooms);
        updated.remove(id);
        write(updated);
        replaceContents(updated);
    }

    public Room cari(String id) {
        Room room = rooms.get(id);
        if (room == null) {
            throw new DataNotFoundException("Kamar tidak ditemukan: " + id);
        }
        return room;
    }

    public List<Room> tampil() {
        return new ArrayList<>(rooms.values());
    }

    public void simpan() {
        write(rooms);
    }

    public void muat() {
        if (!Files.exists(file)) {
            rooms.clear();
            return;
        }

        Map<String, Room> loaded = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    throw new IOException("Format data kamar tidak valid pada baris " + lineNumber + ".");
                }
                Room room;
                try {
                    String number = fields[0].trim();
                    int floor = Integer.parseInt(fields[1].trim());
                    double basePrice = Double.parseDouble(fields[3].trim());
                    RoomStatus status = RoomStatus.valueOf(fields[4].trim());
                    room = createRoom(number, floor, basePrice, status, fields[2].trim());
                } catch (IllegalArgumentException | HotelException ex) {
                    throw new IOException("Data kamar tidak valid pada baris " + lineNumber + ".", ex);
                }
                if (loaded.putIfAbsent(room.getNomor(), room) != null) {
                    throw new IOException("Nomor kamar duplikat pada baris " + lineNumber + ": " + room.getNomor());
                }
            }
        } catch (IOException ex) {
            throw new HotelException("Gagal memuat data kamar dari " + file + ".", ex);
        }
        replaceContents(loaded);
    }

    @Override
    public void add(Room room) {
        tambah(room);
    }

    @Override
    public void update(Room room) {
        ubah(room);
    }

    @Override
    public void delete(String id) {
        hapus(id);
    }

    @Override
    public Optional<Room> findById(String id) {
        try {
            return Optional.of(cari(id));
        } catch (DataNotFoundException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<Room> findAll() {
        return tampil();
    }

    @Override
    public void save() {
        simpan();
    }

    @Override
    public void load() {
        muat();
    }

    private void write(Map<String, Room> source) {
        Path absoluteFile = file.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        Path temporaryFile = null;
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            temporaryFile = Files.createTempFile(parent, absoluteFile.getFileName().toString(), ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                for (Room room : source.values()) {
                    validateCsvValue(room.getNomor());
                    writer.write(room.getNomor());
                    writer.write(',');
                    writer.write(Integer.toString(room.getLantai()));
                    writer.write(',');
                    writer.write(room.getTipe());
                    writer.write(',');
                    writer.write(Double.toString(room.getHargaDasar()));
                    writer.write(',');
                    writer.write(room.getStatus().name());
                    writer.newLine();
                }
            }
            try {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new HotelException("Gagal menyimpan data kamar ke " + file + ".", ex);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ex) {
                    throw new HotelException("Gagal membersihkan file sementara data kamar.", ex);
                }
            }
        }
    }

    private Room createRoom(String number, int floor, double basePrice, RoomStatus status, String type) {
        switch (type.toUpperCase(java.util.Locale.ROOT)) {
            case "STANDARD":
                return new StandardRoom(number, floor, basePrice, status);
            case "DELUXE":
                return new DeluxeRoom(number, floor, basePrice, status);
            case "SUITE":
                return new SuiteRoom(number, floor, basePrice, status);
            default:
                throw new IllegalArgumentException("Tipe kamar tidak dikenal: " + type);
        }
    }

    private void validateCsvValue(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            throw new InvalidInputException("Nomor kamar tidak boleh mengandung koma, tanda kutip, atau baris baru.");
        }
    }

    private void replaceContents(Map<String, Room> replacement) {
        rooms.clear();
        rooms.putAll(replacement);
    }
}