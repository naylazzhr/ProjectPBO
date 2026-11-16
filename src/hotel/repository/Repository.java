package hotel.repository;

import java.util.List;

/**
 * Kontrak generik untuk semua repository (Room, User, Guest, dst).
 * Konsep OOP: interface + generics.
 *
 * @param <T> tipe entitas yang disimpan (misalnya Room)
 */
public interface Repository<T> {

    /** Menambah data baru. Gagal (InvalidInputException) kalau ID sudah ada. */
    void tambah(T item);

    /** Mengubah data yang sudah ada. Gagal (DataNotFoundException) kalau tidak ada. */
    void ubah(T item);

    /** Menghapus data berdasarkan ID. Gagal (DataNotFoundException) kalau tidak ada. */
    void hapus(String id);

    /** Mencari data berdasarkan ID. Gagal (DataNotFoundException) kalau tidak ada. */
    T cari(String id);

    /** Mengambil semua data. */
    List<T> tampil();

    /** Menulis data di memori ke file CSV. */
    void simpan();

    /** Membaca data dari file CSV ke memori. */
    void muat();
}