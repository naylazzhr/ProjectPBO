package com.grandluxury.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class GuestPanel extends JPanel {
    private DefaultTableModel guestModel;
    private JTextField txtNama, txtNik, txtTelp, txtEmail;
    private JComboBox<String> cmbKategori;

    public GuestPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Form Registrasi Tamu Baru
        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 12));

        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setBackground(Color.WHITE);
        formHeader.add(new JLabel("<html><b style='font-size:15px;'>Form Registrasi Tamu Baru</b><br><span style='color:#64748B;font-size:11px;'>Lengkapi data pribadi tamu sebelum membuat reservasi aktif.</span></html>"), BorderLayout.WEST);
        JLabel badgeId = new JLabel(" ID AUTO: GUEST-2026-089 ");
        badgeId.setOpaque(true);
        badgeId.setBackground(Theme.INPUT_BG);
        badgeId.setFont(Theme.FONT_SMALL);
        formHeader.add(badgeId, BorderLayout.EAST);

        JPanel fieldsGrid = new JPanel(new GridLayout(2, 3, 14, 10));
        fieldsGrid.setBackground(Color.WHITE);

        txtNama = Theme.createStyledTextField("Contoh: Bpk. Hendra Gunawan");
        txtNik = Theme.createStyledTextField("Wajib 16 digit angka sesuai KTP");
        txtTelp = Theme.createStyledTextField("+62 812-3456-7890");
        txtEmail = Theme.createStyledTextField("guest@domain.com");
        cmbKategori = new JComboBox<>(new String[]{"Regular Guest", "VIP Executive", "Corporate"});

        JButton btnDaftar = Theme.createPrimaryButton("+ Daftarkan Tamu Baru", Theme.CYAN_ACCENT, Color.WHITE);
        btnDaftar.addActionListener(e -> simpanTamu());

        fieldsGrid.add(wrapField("Nama Lengkap Tamu *", txtNama));
        fieldsGrid.add(wrapField("Nomor Induk Kependudukan (NIK 16 Digit) *", txtNik));
        fieldsGrid.add(wrapField("No. Telepon / WhatsApp *", txtTelp));
        fieldsGrid.add(wrapField("Alamat Email *", txtEmail));
        fieldsGrid.add(wrapField("Kategori Keanggotaan", cmbKategori));
        fieldsGrid.add(wrapField("Aksi", btnDaftar));

        formCard.add(formHeader, BorderLayout.NORTH);
        formCard.add(fieldsGrid, BorderLayout.CENTER);

        // 2. Tabel Daftar Tamu Terdaftar
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(new JLabel("<html><b>Daftar Tamu Terdaftar</b></html>"), BorderLayout.NORTH);

        String[] cols = {"ID TAMU", "NAMA LENGKAP", "KATEGORI", "NIK / IDENTITAS", "KONTAK (TELP & EMAIL)", "KUNJUNGAN"};
        Object[][] rows = {
                {"GUEST-2026-014", "Bpk. Hendra Gunawan", "VIP Executive", "3171040000000002", "+62 811-9234-889 | h.gunawan@aditama.co.id", "14x"},
                {"GUEST-2026-029", "Ibu Clarissa Wijaya", "VIP Executive", "3273150000000005", "+62 813-8822-9011 | clarissa.wijaya@investama.id", "9x"},
                {"GUEST-2026-045", "Michael Anderson", "Corporate", "E482910000000192", "+61 412 890 231 | m.anderson@riotinto.com", "5x"},
                {"GUEST-2026-067", "Siti Lestari", "Regular Guest", "3301090000000003", "+62 856-7890-1234 | siti.lestari94@gmail.com", "1x"}
        };
        guestModel = new DefaultTableModel(rows, cols);
        JTable tblGuest = new JTable(guestModel);
        Theme.styleTable(tblGuest);

        tableCard.add(new JScrollPane(tblGuest), BorderLayout.CENTER);

        add(formCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    private JPanel wrapField(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);
        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void simpanTamu() {
        String nama = txtNama.getText().trim();
        String nik = txtNik.getText().trim();
        String telp = txtTelp.getText().trim();
        // Validasi Input FR-17: NIK wajib 16 digit
        if (nama.isEmpty() || !nik.matches("\\d{16}")) {
            JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong dan NIK wajib 16 digit angka!", "Validasi NIK Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String newId = "GUEST-2026-0" + (guestModel.getRowCount() + 89);
        guestModel.addRow(new Object[]{newId, nama, cmbKategori.getSelectedItem(), nik, telp + " | " + txtEmail.getText(), "1x"});
        txtNama.setText("");
        txtNik.setText("");
        txtTelp.setText("");
        txtEmail.setText("");
        JOptionPane.showMessageDialog(this, "Data tamu berhasil didaftarkan dengan ID: " + newId);
    }
}