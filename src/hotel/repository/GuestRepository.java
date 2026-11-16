package com.grandluxury.repository;

import com.grandluxury.model.Guest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class GuestRepository implements Repository<Guest, String> {
    private final Path file;
    private final Map<String, Guest> guests = new LinkedHashMap<>();

    public GuestRepository() {
        this(Paths.get("data", "guests.csv"));
    }

    public GuestRepository(Path file) {
        this.file = Objects.requireNonNull(file);
    }

    @Override
    public void add(Guest guest) throws IOException {
        if (guests.containsKey(guest.getId())) {
            throw new IllegalArgumentException("ID tamu sudah terdaftar: " + guest.getId());
        }
        requireUniqueNationalId(guest);
        Map<String, Guest> updated = new LinkedHashMap<>(guests);
        updated.put(guest.getId(), guest);
        persist(updated);
        guests.put(guest.getId(), guest);
    }

    @Override
    public void update(Guest guest) throws IOException {
        if (!guests.containsKey(guest.getId())) {
            throw new NoSuchElementException("Tamu tidak ditemukan: " + guest.getId());
        }
        requireUniqueNationalId(guest);
        Map<String, Guest> updated = new LinkedHashMap<>(guests);
        updated.put(guest.getId(), guest);
        persist(updated);
        guests.put(guest.getId(), guest);
    }

    @Override
    public void delete(String id) throws IOException {
        if (!guests.containsKey(id)) {
            throw new NoSuchElementException("Tamu tidak ditemukan: " + id);
        }
        Map<String, Guest> updated = new LinkedHashMap<>(guests);
        updated.remove(id);
        persist(updated);
        guests.remove(id);
    }

    @Override
    public Optional<Guest> findById(String id) {
        return Optional.ofNullable(guests.get(id));
    }

    public Optional<Guest> findByNationalId(String nationalId) {
        return guests.values().stream()
                .filter(guest -> guest.getNationalId().equals(nationalId))
                .findFirst();
    }

    public List<Guest> findByName(String name) {
        String query = name.trim().toLowerCase(Locale.ROOT);
        List<Guest> matches = new ArrayList<>();
        for (Guest guest : guests.values()) {
            if (guest.getName().toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(guest);
            }
        }
        return matches;
    }

    @Override
    public List<Guest> findAll() {
        return new ArrayList<>(guests.values());
    }

    @Override
    public void save() throws IOException {
        persist(guests);
    }

    @Override
    public void load() throws IOException {
        Map<String, Guest> loaded = new LinkedHashMap<>();
        Set<String> nationalIds = new HashSet<>();
        for (List<String> row : CsvSupport.read(file, 6)) {
            Guest guest;
            try {
                guest = new Guest(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5));
            } catch (IllegalArgumentException ex) {
                throw new IOException("Data tamu tidak valid di " + file + ".", ex);
            }
            if (loaded.putIfAbsent(guest.getId(), guest) != null) {
                throw new IOException("ID tamu duplikat di " + file + ": " + guest.getId());
            }
            if (!nationalIds.add(guest.getNationalId())) {
                throw new IOException("NIK duplikat di " + file + ": " + guest.getNationalId());
            }
        }
        guests.clear();
        guests.putAll(loaded);
    }

    private void persist(Map<String, Guest> source) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        for (Guest guest : source.values()) {
            rows.add(List.of(
                    guest.getId(),
                    guest.getName(),
                    guest.getNationalId(),
                    guest.getPhone(),
                    guest.getEmail(),
                    guest.getCategory()
            ));
        }
        CsvSupport.write(file, rows);
    }

    private void requireUniqueNationalId(Guest candidate) {
        for (Guest existing : guests.values()) {
            if (!existing.getId().equals(candidate.getId())
                    && existing.getNationalId().equals(candidate.getNationalId())) {
                throw new IllegalArgumentException("NIK sudah terdaftar untuk tamu lain.");
            }
        }
    }
}