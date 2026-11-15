package com.grandluxury.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private String selectedRole = "Administrator";
    private JButton btnAdmin, btnResepsionis;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame() {
        setTitle("Login - Hotel GrandLuxury");
        setSize(480, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Theme.BG_LIGHT);
        setContentPane(root);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Theme.BG_LIGHT);
        container.setPreferredSize(new Dimension(380, 520));

        // Logo & Judul Brand
        JLabel lblLogo = new JLabel("★", SwingConstants.CENTER);
        lblLogo.setOpaque(true);
        lblLogo.setBackground(Theme.NAVY_DARK);
        lblLogo.setForeground(Theme.CYAN_ACCENT);
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblLogo.setMaximumSize(new Dimension(54, 54));
        lblLogo.setPreferredSize(new Dimension(54, 54));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("GrandLuxury");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitle.setForeground(Theme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Sistem Manajemen Reservasi Terpadu");
        lblSub.setFont(Theme.FONT_BODY);
        lblSub.setForeground(Theme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        container.add(lblLogo);
        container.add(Box.createVerticalStrut(12));
        container.add(lblTitle);
        container.add(Box.createVerticalStrut(4));
        container.add(lblSub);
        container.add(Box.createVerticalStrut(24));

        // Card Login
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRoleHeader = new JLabel("AKSESIBILITAS PERAN");
        lblRoleHeader.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblRoleHeader.setForeground(Theme.TEXT_MUTED);
        lblRoleHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Toggle Peran (Administrator vs Resepsionis)
        JPanel rolePanel = new JPanel(new GridLayout(1, 2, 4, 0));
        rolePanel.setBackground(Theme.INPUT_BG);
        rolePanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        rolePanel.setMaximumSize(new Dimension(350, 42));
        rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnAdmin = new JButton("Administrator");
        btnResepsionis = new JButton("Resepsionis");
        styleRoleTab(btnAdmin, true);
        styleRoleTab(btnResepsionis, false);

        btnAdmin.addActionListener(e -> {
            selectedRole = "Administrator";
            styleRoleTab(btnAdmin, true);
            styleRoleTab(btnResepsionis, false);
            txtUsername.setText("admin@grandluxury.com");
        });

        btnResepsionis.addActionListener(e -> {
            selectedRole = "Resepsionis";
            styleRoleTab(btnAdmin, false);
            styleRoleTab(btnResepsionis, true);
            txtUsername.setText("resepsionis@grandluxury.com");
        });

        rolePanel.add(btnAdmin);
        rolePanel.add(btnResepsionis);

        // Input Username
        JLabel lblUser = new JLabel("Username / ID Pegawai");
        lblUser.setFont(Theme.FONT_BOLD);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = Theme.createStyledTextField("Masukkan Username");
        txtUsername.setText("admin@grandluxury.com");
        txtUsername.setMaximumSize(new Dimension(350, 40));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Input Password
        JLabel lblPass = new JLabel("Kata Sandi");
        lblPass.setFont(Theme.FONT_BOLD);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(Theme.FONT_BODY);
        txtPassword.setBackground(Theme.INPUT_BG);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER_GRAY, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        txtPassword.setMaximumSize(new Dimension(350, 40));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Checkbox & Info Sesi
        JPanel optPanel = new JPanel(new BorderLayout());
        optPanel.setBackground(Theme.CARD_WHITE);
        optPanel.setMaximumSize(new Dimension(350, 30));
        optPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox chkRemember = new JCheckBox("Ingat sesi ini di perangkat");
        chkRemember.setBackground(Theme.CARD_WHITE);
        chkRemember.setFont(Theme.FONT_SMALL);
        chkRemember.setSelected(true);

        JLabel lblShift = new JLabel("● SESI: SIANG");
        lblShift.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblShift.setForeground(Theme.CYAN_ACCENT);

        optPanel.add(chkRemember, BorderLayout.WEST);
        optPanel.add(lblShift, BorderLayout.EAST);

        // Tombol Masuk
        JButton btnLogin = Theme.createPrimaryButton("Masuk ke Sistem  →", Theme.NAVY_DARK, Color.WHITE);
        btnLogin.setMaximumSize(new Dimension(350, 44));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> prosesLogin());

        card.add(lblRoleHeader);
        card.add(Box.createVerticalStrut(6));
        card.add(rolePanel);
        card.add(Box.createVerticalStrut(16));
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(14));
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(12));
        card.add(optPanel);
        card.add(Box.createVerticalStrut(18));
        card.add(btnLogin);

        container.add(card);
        container.add(Box.createVerticalStrut(18));

        JLabel lblFooter = new JLabel("© 2026 Hotel GrandLuxury International.");
        lblFooter.setFont(Theme.FONT_SMALL);
        lblFooter.setForeground(Theme.TEXT_MUTED);
        lblFooter.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(lblFooter);

        root.add(container);
    }

    private void styleRoleTab(JButton btn, boolean active) {
        btn.setFont(Theme.FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Theme.TEXT_DARK);
        } else {
            btn.setBackground(Theme.INPUT_BG);
            btn.setForeground(Theme.TEXT_MUTED);
        }
    }

    private void prosesLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Kata Sandi wajib diisi!", "Validasi Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Simulasi Login Sukses -> Buka MainFrame sesuai Peran
        dispose();
        new MainFrame("Diana Pratiwi", selectedRole).setVisible(true);
    }
}