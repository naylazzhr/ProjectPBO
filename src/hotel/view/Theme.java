package com.grandluxury.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class Theme {
    // Palet Warna Sesuai Prototype
    public static final Color NAVY_DARK   = new Color(1, 28, 64);     // #011C40 (Sidebar & Primary Button)
    public static final Color CYAN_ACCENT = new Color(15, 181, 186);  // #0FB5BA (Aksen & Tombol Tambah)
    public static final Color TEAL_BADGE  = new Color(13, 148, 136);  // #0D9488
    public static final Color BG_LIGHT    = new Color(248, 250, 252); // #F8FAFC (Latar Utama)
    public static final Color CARD_WHITE  = Color.WHITE;
    public static final Color INPUT_BG    = new Color(241, 245, 249); // #F1F5F9
    public static final Color BORDER_GRAY = new Color(226, 232, 240); // #E2E8F0
    public static final Color TEXT_DARK   = new Color(15, 23, 42);    // #0F172A
    public static final Color TEXT_MUTED  = new Color(100, 116, 139); // #64748B
    public static final Color DANGER_RED  = new Color(220, 38, 38);   // #DC2626

    // Tipografi
    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_SUB     = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BOLD    = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);

    public static JButton createPrimaryButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FONT_BODY);
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_DARK);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_GRAY, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setToolTipText(placeholder);
        return field;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_GRAY, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(FONT_BODY);
        table.setGridColor(BORDER_GRAY);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_LIGHT);
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        DefaultTableCellRenderer paddingRenderer = new DefaultTableCellRenderer();
        paddingRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(paddingRenderer);
        }
    }
}