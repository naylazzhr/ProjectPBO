package com.grandluxury.model;

import java.util.Objects;

public final class Guest {
    private final String id;
    private final String name;
    private final String nationalId;
    private final String phone;
    private final String email;
    private final String category;

    public Guest(String id, String name, String nationalId, String phone, String email, String category) {
        this.id = required(id, "ID tamu");
        this.name = required(name, "Nama tamu");
        this.nationalId = required(nationalId, "NIK");
        this.phone = required(phone, "Nomor telepon");
        this.email = required(email, "Email");
        this.category = required(category, "Kategori tamu");

        if (!this.nationalId.matches("\\d{16}")) {
            throw new IllegalArgumentException("NIK harus terdiri dari 16 digit angka.");
        }
        if (!this.phone.matches("[+0-9() .-]{6,24}")) {
            throw new IllegalArgumentException("Format nomor telepon tidak valid.");
        }
        if (!this.email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Format email tidak valid.");
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getCategory() {
        return category;
    }
}