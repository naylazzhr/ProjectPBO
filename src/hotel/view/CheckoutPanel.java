package com.grandluxury.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CheckoutPanel extends JPanel {
    private DefaultTableModel model;
    private JTable table;

    public CheckoutPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Header & Tombol Aksi
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.BG_LIGHT);
        topBar.add(new JLabel("<html><span style='color:#0FB5BA;font-size:10px;font-weight:bold;'>FRONT OFFICE & KASIR RESEPSIONIS</span><br>" +
                "<b style='font-size:18px;'>Operasional Check-In, Check-Out & Kasir</b></html>"), BorderLayout.WEST);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGroup.setBackground(Theme.BG_LIGHT);
        JButton btnCheckIn = Theme.createPrimaryButton("Proses Check-In", Theme.TEAL_BADGE, Color.WHITE);
        JButton btnCheckOut = Theme.createPrimaryButton("Proses Check-Out & Invoice", Theme.NAVY_DARK, Color.WHITE);

        btnCheckIn.addActionListener(e -> prosesCheckIn());
        btnCheckOut.addActionListener(e -> prosesCheckOut());

        actionGroup.add(btnCheckIn);
        actionGroup.add(btnCheckOut);
        topBar.add(actionGroup, BorderLayout.EAST);

        // 2. Tabel Daftar Reservasi Aktif
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        String[] cols = {"NO. RESERVASI", "NAMA TAMU", "TIPE & NO. KAMAR", "PERIODE MENGINAP", "TOTAL TAGIHAN", "STATUS", "METODE BAYAR"};
        Object[][] rows = {
                {"RSV-8821", "Bambang Wicaksono", "Suite 502", "3 Malam", "Rp 3.937.500", "BOOKED", "QRIS"},
                {"RSV-8814", "Clara Stephanie", "Deluxe 310", "2 Malam", "Rp 1.950.000", "CHECKED-IN", "Transfer"},
                {"RSV-8819", "Arthur Pendelton", "Suite 701", "5 Malam", "Rp 6.562.500", "CHECKED-IN", "Transfer"},
                {"RSV-8824", "Hendra Nugroho", "Standard 204", "1 Malam", "Rp 750.000", "BOOKED", "Tunai"},
                {"RSV-8802", "dr. Dinda Wardani", "Deluxe 402", "3 Malam", "Rp 2.925.000", "CHECKED-OUT", "QRIS"}
        };
        model = new DefaultTableModel(rows, cols);
        table = new JTable(model);
        Theme.styleTable(table);

        tableCard.add(new JLabel("<html><b>Daftar Reservasi Aktif & Transaksi Kasir</b> (Pilih baris lalu klik tombol aksi di kanan atas)</html>"), BorderLayout.NORTH);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    private void prosesCheckIn() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris reservasi terlebih dahulu!");
            return;
        }
        String status = model.getValueAt(row, 5).toString();
        if (!status.equals("BOOKED")) { // BR-04
            JOptionPane.showMessageDialog(this, "Check-In hanya berlaku untuk reservasi berstatus BOOKED!", "Aturan Bisnis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        model.setValueAt("CHECKED-IN", row, 5);
        JOptionPane.showMessageDialog(this, "Check-In Berhasil! Status kamar diubah menjadi OCCUPIED.");
    }

    private void prosesCheckOut() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris reservasi terlebih dahulu!");
            return;
        }
        String status = model.getValueAt(row, 5).toString();
        if (!status.equals("CHECKED-IN")) { // BR-04
            JOptionPane.showMessageDialog(this, "Check-Out hanya berlaku untuk tamu berstatus CHECKED-IN!", "Aturan Bisnis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] methods = {"Tunai", "Transfer", "QRIS"};
        String metode = (String) JOptionPane.showInputDialog(this, "Pilih Metode Pembayaran:", "Settlement Kasir",
                JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);

        if (metode != null) {
            model.setValueAt("CHECKED-OUT", row, 5);
            model.setValueAt(metode, row, 6);

            // Cetak Invoice Pop-Up (FR-15)
            String invoice = "========================================\n" +
                    "       HOTEL GRANDLUXURY LAMONGAN       \n" +
                    "            INVOICE PEMBAYARAN          \n" +
                    "========================================\n" +
                    "No. Reservasi : " + model.getValueAt(row, 0) + "\n" +
                    "Nama Tamu     : " + model.getValueAt(row, 1) + "\n" +
                    "Unit Kamar    : " + model.getValueAt(row, 2) + "\n" +
                    "Durasi Inap   : " + model.getValueAt(row, 3) + "\n" +
                    "Metode Bayar  : " + metode + "\n" +
                    "----------------------------------------\n" +
                    "TOTAL LUNAS   : " + model.getValueAt(row, 4) + "\n" +
                    "========================================\n" +
                    "  Terima kasih atas kunjungan Anda!     ";

            JTextArea area = new JTextArea(invoice);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Invoice Pembayaran (FR-15)", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}