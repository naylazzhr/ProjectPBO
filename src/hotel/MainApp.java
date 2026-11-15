package com.grandluxury;

import com.grandluxury.view.LoginFrame;
import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        try {
            // Mengaktifkan tampilan modern sistem operasi
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}