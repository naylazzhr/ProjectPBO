package com.grandluxury.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RoomPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTextField txtNomorKamar, txtHargaDasar, txtSearch;
    private JComboBox<String> cmbLantai, cmbTipe;

    public RoomPanel() {
        setLayout(new BorderLayout(18, 16));
        setBackground(Theme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header Panel
        JPanel headerCard = Theme.createCardPanel();
        headerCard.setLayout(new BorderLayout());
        headerCard.add(new JLabel("<html><span style='color:#0FB5BA;font-size:10px;font-weight:bold;'>● Akses Khusus Administrator</span><br>" +
                "<b style='font-size:16px;'>Konfigurasi & Inventaris Unit Hotel</b></html>"), BorderLayout.WEST);
        headerCard.add(new JLabel("Total Kapasitas: 120 Kamar  |  Aktif Operasional: 96.7%"), BorderLayout.EAST);
        add(headerCard, BorderLayout.NORTH);

        // Kiri: Form Data Unit Kamar
        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(340, getHeight()));

        JLabel lblFormTitle = new JLabel("Data Unit Kamar (Baru / Edit)");
        lblFormTitle.setFont(Theme.FONT_SUB);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtNomorKamar = Theme.createStyledTextField("Contoh: 304");
        txtNomorKamar.setMaximumSize(new Dimension(310, 38));
        txtNomorKamar.setAlignmentX(Component.LEFT_ALIGNMENT);

        cmbLantai = new JComboBox<>(new String[]{"Lantai 1", "Lantai 2", "Lantai 3", "Lantai 4", "Lantai 5"});
        cmbLantai.setMaximumSize(new Dimension(310, 38));
        cmbLantai.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Aturan Tarif Sesuai BR-06: Standard (Dasar), Deluxe (+30%), Suite (+75%)
        cmbTipe = new JComboBox<>(new String[]{"Standard (Tarif Dasar)", "Deluxe (+30% Kompensasi)", "Suite (+75% Kompensasi)"});
        cmbTipe.setMaximumSize(new Dimension(310, 38));
        cmbTipe.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtHargaDasar = Theme.createStyledTextField("750000");
        txtHargaDasar.setText("750000");
        txtHargaDasar.setMaximumSize(new Dimension(310, 38));
        txtHargaDasar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox chkTv = new JCheckBox("Smart TV 55\" 4K", true);
        JCheckBox chkBalcony = new JCheckBox("Balkon Pemandangan", true);
        chkTv.setBackground(Color.WHITE);
        chkBalcony.setBackground(Color.WHITE);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setMaximumSize(new Dimension(310, 40));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSimpan = Theme.createPrimaryButton("Simpan Data", Theme.NAVY_DARK, Color.WHITE);
        JButton btnReset = Theme.createPrimaryButton("Reset Form", Theme.INPUT_BG, Theme.TEXT_DARK);

        btnSimpan.addActionListener(e -> tambahKamar());
        btnReset.addActionListener(e -> {
            txtNomorKamar.setText("");
            txtHargaDasar.setText("750000");
        });

        btnRow.add(btnSimpan);
        btnRow.add(btnReset);

        formCard.add(lblFormTitle);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(new JLabel("Nomor Kamar *"));
        formCard.add(txtNomorKamar);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(new JLabel("Lantai *"));
        formCard.add(cmbLantai);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(new JLabel("Tipe Unit Kamar *"));
        formCard.add(cmbTipe);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(new JLabel("Harga Dasar per Malam (Rp) *"));
        formCard.add(txtHargaDasar);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(chkTv);
        formCard.add(chkBalcony);
        formCard.add(Box.createVerticalStrut(18));
        formCard.add(btnRow);

        // Kanan: Tabel Daftar Kamar
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(Color.WHITE);
        txtSearch = Theme.createStyledTextField("Cari nomor kamar atau tipe...");
        JButton btnHapus = Theme.createPrimaryButton("Hapus Terpilih", Theme.DANGER_RED, Color.WHITE);
        btnHapus.addActionListener(e -> {
            int row = ((JTable) ((JScrollPane) tableCard.getComponent(1)).getViewport().getView()).getSelectedRow();
            if (row >= 0) tableModel.removeRow(row);
            else JOptionPane.showMessageDialog(this, "Pilih baris kamar terlebih dahulu!");
        });

        toolbar.add(txtSearch, BorderLayout.CENTER);
        toolbar.add(btnHapus, BorderLayout.EAST);

        String[] cols = {"NO. KAMAR", "LANTAI", "TIPE KAMAR", "HARGA PER MALAM", "STATUS KAMAR", "FASILITAS SINGKAT"};
        Object[][] initData = {
                {"301", "Lantai 3", "Standard", "Rp 750.000", "Tersedia", "Wi-Fi 6, 50\" TV, City View"},
                {"302", "Lantai 3", "Deluxe", "Rp 975.000", "Terisi", "Twin Bed, Balcony, Breakfast"},
                {"303", "Lantai 3", "Standard", "Rp 750.000", "Perawatan", "AC Deep Cleaning"},
                {"405", "Lantai 4", "Suite", "Rp 1.312.500", "Tersedia", "Jacuzzi, Lounge Access"},
                {"502", "Lantai 5", "Deluxe", "Rp 1.250.000", "Tersedia", "Skyline View, High Floor"}
        };
        tableModel = new DefaultTableModel(initData, cols);
        JTable table = new JTable(tableModel);
        Theme.styleTable(table);

        tableCard.add(toolbar, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        add(formCard, BorderLayout.WEST);
        add(tableCard, BorderLayout.CENTER);
    }

    private void tambahKamar() {
        String no = txtNomorKamar.getText().trim();
        String hargaStr = txtHargaDasar.getText().trim();
        if (no.isEmpty() || hargaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor kamar dan harga wajib diisi!", "Input Invalid", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            double dasar = Double.parseDouble(hargaStr);
            String tipeFull = (String) cmbTipe.getSelectedItem();
            String tipe = tipeFull.split(" ")[0];
            double tarif = switch (tipe) {
                case "Deluxe" -> dasar * 1.30; // BR-06
                case "Suite"  -> dasar * 1.75; // BR-06
                default       -> dasar;
            };
            tableModel.addRow(new Object[]{no, cmbLantai.getSelectedItem(), tipe, String.format("Rp %,.0f", tarif), "Tersedia", "Smart TV, Wi-Fi 6"});
            txtNomorKamar.setText("");
            JOptionPane.showMessageDialog(this, "Unit kamar " + no + " berhasil disimpan!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Harga dasar harus berupa angka!", "Error Format", JOptionPane.ERROR_MESSAGE);
        }
    }
}