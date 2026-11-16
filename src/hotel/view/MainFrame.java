package com.grandluxury.view;

import com.grandluxury.repository.GuestRepository;
import com.grandluxury.repository.ReservationRepository;
import com.grandluxury.service.ReservationService;
import com.grandluxury.service.RoomRepositoryGateway;
import hotel.repository.RoomRepository;
import hotel.util.HotelException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private RoomPanel roomPanel;
    private Map<String, JButton> menuButtons = new LinkedHashMap<>();
    private String currentUser;
    private String currentRole;

    public MainFrame(String user, String role) {
        this.currentUser = user;
        this.currentRole = role;

        GuestRepository guestRepository = new GuestRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        RoomRepository roomRepository = new RoomRepository();
        try {
            guestRepository.load();
            reservationRepository.load();
            roomRepository.muat();
        } catch (IOException | HotelException ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Data aplikasi gagal dimuat: " + ex.getMessage(),
                    "Kesalahan Data",
                    JOptionPane.ERROR_MESSAGE
            );
            throw new IllegalStateException("Data aplikasi gagal dimuat.", ex);
        }
        ReservationService reservationService = new ReservationService(
                guestRepository,
                reservationRepository,
                new RoomRepositoryGateway(roomRepository)
        );

        setTitle("GrandLuxury Hotel & Residences - Sistem Reservasi");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // 1. SIDEBAR KIRI (DARK NAVY)
        root.add(buildSidebar(), BorderLayout.WEST);

        // 2. AREA KANAN (TOPBAR + KONTEN DINAMIS)
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(Theme.BG_LIGHT);
        rightWrapper.add(buildTopbar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.BG_LIGHT);

        // Daftarkan 5 Panel Utama Sesuai Prototype
        contentPanel.add(new DashboardPanel(this::switchPage), "DASHBOARD");
        roomPanel = new RoomPanel(roomRepository);
        contentPanel.add(roomPanel, "KAMAR");
        contentPanel.add(new GuestPanel(guestRepository), "TAMU");
        contentPanel.add(new ReservationPanel(reservationService), "RESERVASI");
        contentPanel.add(new CheckoutPanel(), "CHECKOUT");

        rightWrapper.add(contentPanel, BorderLayout.CENTER);
        root.add(rightWrapper, BorderLayout.CENTER);

        switchPage("DASHBOARD");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.NAVY_DARK);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 16, 20, 16));

        // Brand Header
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandBox.setBackground(Theme.NAVY_DARK);
        brandBox.setMaximumSize(new Dimension(230, 50));
        brandBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel("♛", SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(Color.WHITE);
        icon.setForeground(Theme.NAVY_DARK);
        icon.setFont(new Font("SansSerif", Font.BOLD, 18));
        icon.setPreferredSize(new Dimension(36, 36));

        JLabel brandText = new JLabel("<html><b style='color:white;font-size:13px;'>GrandLuxury</b><br><span style='color:#94A3B8;font-size:8px;'>HOTEL & RESIDENCES</span></html>");
        brandBox.add(icon);
        brandBox.add(brandText);

        sidebar.add(brandBox);
        sidebar.add(Box.createVerticalStrut(28));

        JLabel lblMenuGroup = new JLabel("MENU OPERASIONAL");
        lblMenuGroup.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblMenuGroup.setForeground(new Color(148, 163, 184));
        lblMenuGroup.setBorder(new EmptyBorder(0, 8, 8, 0));
        lblMenuGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblMenuGroup);

        // Item Navigasi
        addNavButton(sidebar, "DASHBOARD", "⊞  Dashboard Utama", true);
        // Kontrol Hak Akses Peran (FR-01 & FR-02): Manajemen Kamar khusus Administrator
        boolean isAdmin = currentRole.equalsIgnoreCase("Administrator");
        addNavButton(sidebar, "KAMAR", "🛏  Manajemen Kamar", isAdmin);
        addNavButton(sidebar, "TAMU", "👥  Data Tamu", true);
        addNavButton(sidebar, "RESERVASI", "📅  Reservasi & Cek Kamar", true);
        addNavButton(sidebar, "CHECKOUT", "🧾  Check-In, Check-Out & Invoice", true);

        sidebar.add(Box.createVerticalGlue());

        // Tombol Logout di bawah sidebar
        JButton btnLogout = Theme.createPrimaryButton("Keluar / Ganti Akun", new Color(15, 41, 82), Color.WHITE);
        btnLogout.setMaximumSize(new Dimension(220, 40));
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        sidebar.add(btnLogout);

        return sidebar;
    }

    private void addNavButton(JPanel sidebar, String key, String label, boolean enabled) {
        JButton btn = new JButton(label);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(Theme.FONT_BOLD);
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setEnabled(enabled);

        if (!enabled) {
            btn.setToolTipText("Akses khusus Administrator");
        }

        btn.addActionListener(e -> switchPage(key));
        menuButtons.put(key, btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(6));
    }

    private JPanel buildTopbar() {
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(Color.WHITE);
        topbar.setPreferredSize(new Dimension(getWidth(), 64));
        topbar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, Theme.BORDER_GRAY),
                new EmptyBorder(10, 24, 10, 24)
        ));

        // Search Bar Global
        JTextField txtGlobalSearch = Theme.createStyledTextField("Cari tamu, no. kamar, atau ID booking...");
        txtGlobalSearch.setPreferredSize(new Dimension(360, 38));

        // Profil User Kanan
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 2));
        profilePanel.setBackground(Color.WHITE);

        JLabel lblUser = new JLabel("<html><div style='text-align:right;'><b>" + currentUser + "</b><br>" +
                "<span style='color:#0FB5BA;font-size:9px;font-weight:bold;'>" + currentRole.toUpperCase() + "</span></div></html>");
        JLabel avatar = new JLabel("DP", SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(Theme.NAVY_DARK);
        avatar.setForeground(Color.WHITE);
        avatar.setFont(Theme.FONT_BOLD);
        avatar.setPreferredSize(new Dimension(36, 36));

        profilePanel.add(lblUser);
        profilePanel.add(avatar);

        topbar.add(txtGlobalSearch, BorderLayout.WEST);
        topbar.add(profilePanel, BorderLayout.EAST);
        return topbar;
    }

    public void switchPage(String pageKey) {
        cardLayout.show(contentPanel, pageKey);
        if ("KAMAR".equals(pageKey) && roomPanel != null) {
            roomPanel.refreshTable();
        }
        for (Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(pageKey)) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(Theme.NAVY_DARK);
            } else {
                btn.setBackground(Theme.NAVY_DARK);
                btn.setForeground(new Color(148, 163, 184));
            }
        }
    }
}