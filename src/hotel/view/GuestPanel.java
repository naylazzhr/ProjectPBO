package com.grandluxury.view;

import com.grandluxury.model.Guest;
import com.grandluxury.repository.GuestRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class GuestPanel extends JPanel {
    private final GuestRepository repository;
    private final DefaultTableModel guestModel;
    private final JTable guestTable;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField txtNama;
    private final JTextField txtNik;
    private final JTextField txtTelp;
    private final JTextField txtEmail;
    private final JTextField txtSearch;
    private final JComboBox<String> cmbKategori;
    private String selectedGuestId;

    public GuestPanel() {
        this(new GuestRepository());
    }

    public GuestPanel(GuestRepository repository) {
        this.repository = Objects.requireNonNull(repository);
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 12));
        formCard.add(new JLabel("<html><b style='font-size:15px;'>Data Tamu</b><br>"
                + "<span style='color:#64748B;font-size:11px;'>Tambah dan kelola data tamu hotel.</span></html>"),
                BorderLayout.NORTH);

        JPanel fieldsGrid = new JPanel(new GridLayout(2, 3, 14, 10));
        fieldsGrid.setBackground(Color.WHITE);
        txtNama = Theme.createStyledTextField("Nama lengkap");
        txtNik = Theme.createStyledTextField("16 digit angka");
        txtTelp = Theme.createStyledTextField("Nomor telepon");
        txtEmail = Theme.createStyledTextField("guest@domain.com");
        cmbKategori = new JComboBox<>(new String[]{"Regular Guest", "VIP Executive", "Corporate"});

        JPanel actions = new JPanel(new GridLayout(1, 3, 6, 0));
        actions.setBackground(Color.WHITE);
        JButton btnTambah = Theme.createPrimaryButton("Tambah", Theme.CYAN_ACCENT, Color.WHITE);
        JButton btnUbah = Theme.createPrimaryButton("Ubah", Theme.NAVY_DARK, Color.WHITE);
        JButton btnBersihkan = Theme.createPrimaryButton("Bersihkan", Theme.INPUT_BG, Theme.TEXT_DARK);
        btnTambah.addActionListener(e -> addGuest());
        btnUbah.addActionListener(e -> updateGuest());
        btnBersihkan.addActionListener(e -> clearForm());
        actions.add(btnTambah);
        actions.add(btnUbah);
        actions.add(btnBersihkan);

        fieldsGrid.add(wrapField("Nama Lengkap *", txtNama));
        fieldsGrid.add(wrapField("NIK (16 Digit) *", txtNik));
        fieldsGrid.add(wrapField("Nomor Telepon *", txtTelp));
        fieldsGrid.add(wrapField("Email *", txtEmail));
        fieldsGrid.add(wrapField("Kategori", cmbKategori));
        fieldsGrid.add(wrapField("Aksi", actions));
        formCard.add(fieldsGrid, BorderLayout.CENTER);

        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(Color.WHITE);
        txtSearch = Theme.createStyledTextField("Cari nama, NIK, telepon, atau email");
        JButton btnHapus = Theme.createPrimaryButton("Hapus Terpilih", Theme.DANGER_RED, Color.WHITE);
        JButton btnCari = Theme.createPrimaryButton("Cari", Theme.NAVY_DARK, Color.WHITE);
        btnCari.addActionListener(e -> filterGuests());
        txtSearch.addActionListener(e -> filterGuests());
        btnHapus.addActionListener(e -> deleteGuest());
        toolbar.add(txtSearch, BorderLayout.CENTER);
        toolbar.add(btnCari, BorderLayout.EAST);

        String[] columns = {"ID TAMU", "NAMA", "KATEGORI", "NIK", "TELEPON", "EMAIL"};
        guestModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        guestTable = new JTable(guestModel);
        guestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTable(guestTable);
        sorter = new TableRowSorter<>(guestModel);
        guestTable.setRowSorter(sorter);
        guestTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedGuest();
            }
        });

        JPanel tableHeader = new JPanel(new BorderLayout(8, 0));
        tableHeader.setBackground(Color.WHITE);
        tableHeader.add(toolbar, BorderLayout.CENTER);
        tableHeader.add(btnHapus, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(guestTable), BorderLayout.CENTER);

        add(formCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        try {
            repository.load();
            refreshTable();
        } catch (IOException ex) {
            showError("Gagal memuat data tamu: " + ex.getMessage());
        }
    }

    private JPanel wrapField(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(Color.WHITE);
        JLabel title = new JLabel(label);
        title.setFont(Theme.FONT_SMALL);
        title.setForeground(Theme.TEXT_MUTED);
        panel.add(title, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void addGuest() {
        try {
            String id = "GUEST-" + java.time.Year.now().getValue() + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            repository.add(readGuest(id));
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Data tamu berhasil disimpan dengan ID " + id + ".");
        } catch (IOException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateGuest() {
        if (selectedGuestId == null) {
            showError("Pilih tamu yang akan diubah terlebih dahulu.");
            return;
        }
        try {
            repository.update(readGuest(selectedGuestId));
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Data tamu berhasil diperbarui.");
        } catch (IOException | IllegalArgumentException | NoSuchElementException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteGuest() {
        if (selectedGuestId == null) {
            showError("Pilih tamu yang akan dihapus terlebih dahulu.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Hapus data tamu terpilih?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            repository.delete(selectedGuestId);
            refreshTable();
            clearForm();
        } catch (IOException | NoSuchElementException ex) {
            showError(ex.getMessage());
        }
    }

    private Guest readGuest(String id) {
        return new Guest(
                id,
                txtNama.getText(),
                txtNik.getText(),
                txtTelp.getText(),
                txtEmail.getText(),
                (String) cmbKategori.getSelectedItem()
        );
    }

    private void loadSelectedGuest() {
        int viewRow = guestTable.getSelectedRow();
        if (viewRow < 0) {
            selectedGuestId = null;
            return;
        }
        int row = guestTable.convertRowIndexToModel(viewRow);
        selectedGuestId = guestModel.getValueAt(row, 0).toString();
        txtNama.setText(guestModel.getValueAt(row, 1).toString());
        cmbKategori.setSelectedItem(guestModel.getValueAt(row, 2).toString());
        txtNik.setText(guestModel.getValueAt(row, 3).toString());
        txtTelp.setText(guestModel.getValueAt(row, 4).toString());
        txtEmail.setText(guestModel.getValueAt(row, 5).toString());
    }

    private void refreshTable() {
        guestModel.setRowCount(0);
        for (Guest guest : repository.findAll()) {
            guestModel.addRow(new Object[]{
                    guest.getId(),
                    guest.getName(),
                    guest.getCategory(),
                    guest.getNationalId(),
                    guest.getPhone(),
                    guest.getEmail()
            });
        }
        filterGuests();
    }

    private void filterGuests() {
        String query = txtSearch.getText().trim();
        sorter.setRowFilter(query.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
    }

    private void clearForm() {
        selectedGuestId = null;
        guestTable.clearSelection();
        txtNama.setText("");
        txtNik.setText("");
        txtTelp.setText("");
        txtEmail.setText("");
        cmbKategori.setSelectedIndex(0);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Data Tamu", JOptionPane.ERROR_MESSAGE);
    }
}
