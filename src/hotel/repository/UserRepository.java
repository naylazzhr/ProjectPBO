package hotel.repository;

import hotel.model.Admin;
import hotel.model.Receptionist;
import hotel.model.User;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Menyimpan dan membaca data pengguna (Admin dan Receptionist) dari data/users.csv.
 *
 * Format file (baris pertama adalah judul kolom):
 *   userId,username,password,role,detail
 *   U001,admin,admin123,ADMIN,FULL
 *   U002,resepsionis,resep123,RECEPTIONIST,PAGI
 *
 * Kolom "detail" berisi adminAccessLevel (untuk ADMIN) atau shiftWork (untuk RECEPTIONIST).
 */
public class UserRepository implements Repository<User, String> {

    private static final String HEADER = "userId,username,password,role,detail";

    private final Path file;
    private final Map<String, User> users = new LinkedHashMap<>();

    public UserRepository() {
        this(Paths.get("data", "users.csv"));
    }

    public UserRepository(Path file) {
        this.file = Objects.requireNonNull(file);
    }

    // ---------- Operasi data (nama Indonesia) ----------

    public void tambah(User user) {
        validateUser(user);
        if (users.containsKey(user.getUserId())) {
            throw new InvalidInputException("ID pengguna sudah terdaftar: " + user.getUserId());
        }
        if (usernameDipakai(user.getUsername(), null)) {
            throw new InvalidInputException("Username sudah dipakai: " + user.getUsername());
        }
        Map<String, User> updated = new LinkedHashMap<>(users);
        updated.put(user.getUserId(), user);
        write(updated);
        replaceContents(updated);
    }

    public void ubah(User user) {
        validateUser(user);
        if (!users.containsKey(user.getUserId())) {
            throw new DataNotFoundException("Pengguna tidak ditemukan: " + user.getUserId());
        }
        if (usernameDipakai(user.getUsername(), user.getUserId())) {
            throw new InvalidInputException("Username sudah dipakai: " + user.getUsername());
        }
        Map<String, User> updated = new LinkedHashMap<>(users);
        updated.put(user.getUserId(), user);
        write(updated);
        replaceContents(updated);
    }

    public void hapus(String id) {
        if (!users.containsKey(id)) {
            throw new DataNotFoundException("Pengguna tidak ditemukan: " + id);
        }
        Map<String, User> updated = new LinkedHashMap<>(users);
        updated.remove(id);
        write(updated);
        replaceContents(updated);
    }

    public User cari(String id) {
        User user = users.get(id);
        if (user == null) {
            throw new DataNotFoundException("Pengguna tidak ditemukan: " + id);
        }
        return user;
    }

    public List<User> tampil() {
        return new ArrayList<>(users.values());
    }

    /**
     * Untuk login: mengembalikan pengguna kalau username dan password cocok,
     * atau kosong (Optional.empty) kalau tidak cocok.
     */
    public Optional<User> autentikasi(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        for (User user : users.values()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public void simpan() {
        write(users);
    }

    public void muat() {
        if (!Files.exists(file)) {
            users.clear();
            return;
        }

        Map<String, User> loaded = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (lineNumber == 1 && fields[0].trim().equalsIgnoreCase("userId")) {
                    continue;   // lewati baris judul kolom
                }
                if (fields.length != 5) {
                    throw new IOException("Format data pengguna tidak valid pada baris " + lineNumber + ".");
                }
                User user;
                try {
                    user = createUser(fields[0].trim(), fields[1].trim(), fields[2].trim(),
                            fields[3].trim(), fields[4].trim());
                } catch (IllegalArgumentException | HotelException ex) {
                    throw new IOException("Data pengguna tidak valid pada baris " + lineNumber + ".", ex);
                }
                if (loaded.putIfAbsent(user.getUserId(), user) != null) {
                    throw new IOException("ID pengguna duplikat pada baris " + lineNumber + ": " + user.getUserId());
                }
            }
        } catch (IOException ex) {
            throw new HotelException("Gagal memuat data pengguna dari " + file + ".", ex);
        }
        replaceContents(loaded);
    }

    // ---------- Implementasi interface Repository milik tim (nama Inggris) ----------

    @Override
    public void add(User user) {
        tambah(user);
    }

    @Override
    public void update(User user) {
        ubah(user);
    }

    @Override
    public void delete(String id) {
        hapus(id);
    }

    @Override
    public Optional<User> findById(String id) {
        try {
            return Optional.of(cari(id));
        } catch (DataNotFoundException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
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

    // ---------- Method bantu (private) ----------

    /** Membuat Admin atau Receptionist sesuai kolom role (polymorphism). */
    private User createUser(String userId, String username, String password, String role, String detail) {
        switch (role.toUpperCase(Locale.ROOT)) {
            case "ADMIN":
                return new Admin(userId, username, password, detail);
            case "RECEPTIONIST":
                return new Receptionist(userId, username, password, detail);
            default:
                throw new IllegalArgumentException("Role tidak dikenal: " + role);
        }
    }

    /** Mengambil isi kolom detail dari objek User sesuai tipenya. */
    private String detailOf(User user) {
        String detail = "";
        if (user instanceof Admin) {
            detail = ((Admin) user).getAdminAccessLevel();
        } else if (user instanceof Receptionist) {
            detail = ((Receptionist) user).getShiftWork();
        }
        return detail == null ? "" : detail;
    }

    private boolean usernameDipakai(String username, String kecualiUserId) {
        for (User user : users.values()) {
            boolean orangLain = kecualiUserId == null || !user.getUserId().equals(kecualiUserId);
            if (orangLain && user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private void validateUser(User user) {
        Objects.requireNonNull(user, "Data pengguna tidak boleh null.");
        wajibDiisi(user.getUserId(), "ID pengguna");
        wajibDiisi(user.getUsername(), "Username");
        wajibDiisi(user.getPassword(), "Password");
        validateCsvValue(user.getRole(), "Role");
        validateCsvValue(detailOf(user), "Detail");
    }

    private void wajibDiisi(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(label + " tidak boleh kosong");
        }
        validateCsvValue(value, label);
    }

    /** Koma, tanda kutip, dan baris baru akan merusak format CSV, jadi ditolak. */
    private void validateCsvValue(String value, String label) {
        if (value == null) {
            throw new InvalidInputException(label + " tidak boleh null");
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            throw new InvalidInputException(label + " tidak boleh mengandung koma, tanda kutip, atau baris baru.");
        }
    }

    /**
     * Menulis semua data ke file. Ditulis ke file sementara dulu, baru dipindahkan,
     * supaya users.csv tidak rusak kalau penulisan gagal di tengah jalan.
     */
    private void write(Map<String, User> source) {
        Path absoluteFile = file.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        Path temporaryFile = null;
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            temporaryFile = Files.createTempFile(parent, absoluteFile.getFileName().toString(), ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (User user : source.values()) {
                    validateUser(user);
                    writer.write(user.getUserId());
                    writer.write(',');
                    writer.write(user.getUsername());
                    writer.write(',');
                    writer.write(user.getPassword());
                    writer.write(',');
                    writer.write(user.getRole());
                    writer.write(',');
                    writer.write(detailOf(user));
                    writer.newLine();
                }
            }
            try {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new HotelException("Gagal menyimpan data pengguna ke " + file + ".", ex);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ex) {
                    throw new HotelException("Gagal membersihkan file sementara data pengguna.", ex);
                }
            }
        }
    }

    private void replaceContents(Map<String, User> replacement) {
        users.clear();
        users.putAll(replacement);
    }
}