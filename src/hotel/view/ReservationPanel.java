package com.grandluxury.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReservationPanel extends JPanel {
    private JTextField txtCheckIn, txtCheckOut;
    private JSpinner spnMalam;

    public ReservationPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Filter Jadwal Menginap
        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new GridLayout(1, 4, 14, 0));

        txtCheckIn = Theme.createStyledTextField("2026-11-16");
        txtCheckIn.setText("2026-11-16");
        txtCheckOut = Theme.createStyledTextField("2026-11-19");
        txtCheckOut.setText("2026-11-19");
        spnMalam = new JSpinner(new SpinnerNumberModel(3, 1, 30, 1));

        JButton btnCek = Theme.createPrimaryButton("🔍 Cek Ketersediaan", Theme.CYAN_ACCENT, Color.WHITE);
        btnCek.addActionListener(e -> JOptionPane.showMessageDialog(this, "Menampilkan 3 Kategori Kamar Tersedia untuk " + spnMalam.getValue() + " Malam."));

        filterCard.add(createBox("TANGGAL CHECK-IN", txtCheckIn));
        filterCard.add(createBox("TANGGAL CHECK-OUT", txtCheckOut));
        filterCard.add(createBox("DURASI (MALAM)", spnMalam));
        filterCard.add(createBox("PENCARIAN", btnCek));

        // 2. Katalog Kartu Kamar Tersedia (3 Tier OOP: Standard, Deluxe, Suite)
        JPanel catalogPanel = new JPanel(new GridLayout(3, 1, 0, 14));
        catalogPanel.setBackground(Theme.BG_LIGHT);

        catalogPanel.add(createRoomOfferCard("STANDARD URBAN", "Standard King Room", "King Bed • High-Speed WiFi • Rain Shower", 750000, "12 Kamar Sisa", Theme.NAVY_DARK));
        catalogPanel.add(createRoomOfferCard("DELUXE PANORAMA (+30%)", "Deluxe Executive Skyline", "City View • Private Balcony • Breakfast Included", 975000, "8 Kamar Sisa", Theme.CYAN_ACCENT));
        catalogPanel.add(createRoomOfferCard("SUITE PENTHOUSE (+75%)", "Presidential Suite with Bathtub", "Bathtub Jacuzzi • Lounge Access • 24h Butler", 1312500, "3 Kamar Sisa", Theme.NAVY_DARK));

        add(filterCard, BorderLayout.NORTH);
        add(new JScrollPane(catalogPanel), BorderLayout.CENTER);
    }

    private JPanel createBox(String title, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(title);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(Theme.TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel createRoomOfferCard(String badge, String roomName, String desc, double rate, String stock, Color btnColor) {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(20, 0));

        JLabel info = new JLabel("<html><span style='color:#0FB5BA;font-size:10px;font-weight:bold;'>" + badge + "</span><br>" +
                "<b style='font-size:16px;color:#0F172A;'>" + roomName + "</b><br>" +
                "<span style='color:#64748B;font-size:11px;'>" + desc + "</span></html>");

        JPanel rightAction = new JPanel(new GridLayout(3, 1, 0, 4));
        rightAction.setBackground(Color.WHITE);
        JLabel lblStock = new JLabel("● " + stock, SwingConstants.RIGHT);
        lblStock.setForeground(Theme.TEAL_BADGE);
        lblStock.setFont(Theme.FONT_SMALL);

        JLabel lblPrice = new JLabel(String.format("Rp %,.0f / malam", rate), SwingConstants.RIGHT);
        lblPrice.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton btnBook = Theme.createPrimaryButton("Pilih & Reservasi  →", btnColor, Color.WHITE);
        btnBook.addActionListener(e -> {
            int malam = (int) spnMalam.getValue();
            double total = rate * malam;
            String namaTamu = JOptionPane.showInputDialog(this, "Masukkan Nama / ID Tamu yang memesan:", "Bpk. Hendra Gunawan");
            if (namaTamu != null && !namaTamu.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Reservasi Berhasil Dibuat!\nTamu: " + namaTamu +
                        "\nKamar: " + roomName +
                        "\nDurasi: " + malam + " Malam" +
                        "\nEstimasi Total: " + String.format("Rp %,.0f", total),
                        "Konfirmasi Booking", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        rightAction.add(lblStock);
        rightAction.add(lblPrice);
        rightAction.add(btnBook);

        card.add(info, BorderLayout.CENTER);
        card.add(rightAction, BorderLayout.EAST);
        return card;
    }
}