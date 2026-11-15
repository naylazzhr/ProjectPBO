package com.grandluxury.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Consumer;

public class DashboardPanel extends JPanel {
    public DashboardPanel(Consumer<String> onNavigate) {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Hero Banner Selamat Datang
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Theme.NAVY_DARK);
        banner.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel lblHero = new JLabel("<html><span style='color:#38BDF8;font-size:10px;'>● Hari ini, Minggu 15 November 2026</span><br>" +
                "<b style='color:white;font-size:18px;'>Selamat Datang kembali, Diana</b></html>");
        JButton btnNewRes = Theme.createPrimaryButton("+ Buat Reservasi Baru", Theme.CYAN_ACCENT, Color.WHITE);
        btnNewRes.addActionListener(e -> onNavigate.accept("RESERVASI"));

        banner.add(lblHero, BorderLayout.WEST);
        banner.add(btnNewRes, BorderLayout.EAST);

        // 2. 4 Kartu Statistik Status Kamar
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        statsGrid.setBackground(Theme.BG_LIGHT);
        statsGrid.add(createStatCard("Kamar Tersedia", "48", "/ 120 Total", "Rasio Ketersediaan 40.0%", Theme.TEAL_BADGE));
        statsGrid.add(createStatCard("Kamar Dipesan", "32", "Check-In Est.", "Booking Rate 26.6%", new Color(217, 119, 6)));
        statsGrid.add(createStatCard("Kamar Terisi", "35", "Sedang Menginap", "Okupansi Aktif 29.2%", Theme.NAVY_DARK));
        statsGrid.add(createStatCard("Dalam Perawatan", "5", "Housekeeping", "Non-Operasional 4.2%", Theme.DANGER_RED));

        JPanel topSection = new JPanel(new BorderLayout(0, 16));
        topSection.setBackground(Theme.BG_LIGHT);
        topSection.add(banner, BorderLayout.NORTH);
        topSection.add(statsGrid, BorderLayout.CENTER);

        // 3. Bagian Bawah: Tabel Jadwal Operasional & Matriks Kamar Cepat
        JPanel bottomGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomGrid.setBackground(Theme.BG_LIGHT);

        // Kiri: Jadwal Operasional Tamu
        JPanel scheduleCard = Theme.createCardPanel();
        scheduleCard.setLayout(new BorderLayout(0, 12));
        JLabel lblSchedTitle = new JLabel("<html><b>Jadwal Operasional Tamu</b><br><span style='color:#64748B;font-size:10px;'>Monitoring kedatangan dan kepulangan tamu hari ini.</span></html>");
        scheduleCard.add(lblSchedTitle, BorderLayout.NORTH);

        String[] cols = {"TAMU & ID BOOKING", "KAMAR & TIPE", "JADWAL", "TIPE AKSI"};
        Object[][] data = {
                {"Bambang Wijaya (#GL-240582)", "Room 402 - Suite", "11:30 WIB", "Check-In"},
                {"Aileen Laurent (#GL-240590)", "Room 214 - Deluxe", "12:00 WIB", "Check-Out"},
                {"Rian Hartanto (#GL-240595)", "Room 108 - Standard", "13:15 WIB", "Check-In"},
                {"Klarissa Tan (#GL-240601)", "Room 305 - Suite", "13:45 WIB", "Check-Out"}
        };
        JTable tblSchedule = new JTable(new DefaultTableModel(data, cols));
        Theme.styleTable(tblSchedule);
        scheduleCard.add(new JScrollPane(tblSchedule), BorderLayout.CENTER);

        // Kanan: Matriks Kamar Cepat (Lantai 1 - 4)
        JPanel matrixCard = Theme.createCardPanel();
        matrixCard.setLayout(new BorderLayout(0, 12));
        JLabel lblMatrixTitle = new JLabel("<html><b>Matriks Kamar Cepat</b><br><span style='color:#64748B;font-size:10px;'>Status visual per lantai (Hijau: Tersedia | Kuning: Dipesan | Biru: Terisi)</span></html>");
        matrixCard.add(lblMatrixTitle, BorderLayout.NORTH);

        JPanel floorsPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        floorsPanel.setBackground(Color.WHITE);
        floorsPanel.add(createFloorRow("Lantai 4 • Suite Presidential", new String[]{"401", "402", "403", "404", "405", "406"}, new int[]{0, 1, 0, 0, 3, 2}));
        floorsPanel.add(createFloorRow("Lantai 3 • Club Executive", new String[]{"301", "302", "303", "304", "305", "306"}, new int[]{2, 2, 2, 1, 0, 0}));
        floorsPanel.add(createFloorRow("Lantai 2 • Deluxe King/Twin", new String[]{"201", "202", "203", "204", "205", "206"}, new int[]{1, 1, 0, 0, 2, 0}));
        floorsPanel.add(createFloorRow("Lantai 1 • Superior Garden", new String[]{"101", "102", "103", "104", "105", "106"}, new int[]{0, 0, 0, 2, 1, 0}));

        matrixCard.add(floorsPanel, BorderLayout.CENTER);

        bottomGrid.add(scheduleCard);
        bottomGrid.add(matrixCard);

        add(topSection, BorderLayout.NORTH);
        add(bottomGrid, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, String mainVal, String subVal, String footer, Color accent) {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(0, 8));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.FONT_BOLD);
        lblTitle.setForeground(Theme.TEXT_MUTED);

        JLabel lblVal = new JLabel("<html><b style='font-size:22px;color:#0F172A;'>" + mainVal + "</b> <span style='color:#64748B;font-size:11px;'>" + subVal + "</span></html>");
        JLabel lblFoot = new JLabel(footer);
        lblFoot.setFont(Theme.FONT_SMALL);
        lblFoot.setForeground(accent);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        card.add(lblFoot, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createFloorRow(String floorLabel, String[] rooms, int[] statuses) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(floorLabel);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);

        JPanel grid = new JPanel(new GridLayout(1, rooms.length, 6, 0));
        grid.setBackground(Color.WHITE);
        for (int i = 0; i < rooms.length; i++) {
            JLabel box = new JLabel(rooms[i], SwingConstants.CENTER);
            box.setOpaque(true);
            box.setFont(new Font("SansSerif", Font.BOLD, 11));
            box.setForeground(Color.WHITE);
            switch (statuses[i]) {
                case 0 -> box.setBackground(Theme.TEAL_BADGE);       // Tersedia
                case 1 -> box.setBackground(new Color(234, 179, 8)); // Dipesan
                case 2 -> box.setBackground(Theme.NAVY_DARK);        // Terisi
                default -> box.setBackground(Theme.DANGER_RED);      // Perawatan
            }
            grid.add(box);
        }
        row.add(lbl, BorderLayout.NORTH);
        row.add(grid, BorderLayout.CENTER);
        return row;
    }
}