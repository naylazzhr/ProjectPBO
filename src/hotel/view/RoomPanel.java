package com.grandluxury.view;

import hotel.model.DeluxeRoom;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.model.StandardRoom;
import hotel.model.SuiteRoom;
import hotel.repository.RoomRepository;
import hotel.util.HotelException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Objects;

public class RoomPanel extends JPanel {
    private final RoomRepository repository;
    private final DefaultTableModel tableModel;
    private final JTable roomTable;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField txtNomorKamar;
    private final JTextField txtHargaDasar;
    private final JTextField txtSearch;
    private final JComboBox<String> cmbLantai;
    private final JComboBox<String> cmbTipe;
    private final JButton btnMaintenance;
    private String selectedRoomNumber;

    public RoomPanel() {
        this(new RoomRepository());
    }

    public RoomPanel(RoomRepository repository) {
        this.repository = Objects.requireNonNull(repository);
        setLayout(new BorderLayout(18, 16));
        setBackground(Theme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel headerCard = Theme.createCardPanel();
        headerCard.setLayout(new BorderLayout());
        headerCard.add(new JLabel("<html><span style='color:#0FB5BA;font-size:10px;font-weight:bold;'>● Akses Khusus Administrator</span><br>"
                + "<b style='font-size:16px;'>Konfigurasi & Inventaris Unit Hotel</b></html>"), BorderLayout.WEST);
        headerCard.add(new JLabel("Status kamar tersimpan dan dipakai oleh modul reservasi."), BorderLayout.EAST);
        add(headerCard, BorderLayout.NORTH);

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

        cmbTipe = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite"});
        cmbTipe.setMaximumSize(new Dimension(310, 38));
        cmbTipe.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtHargaDasar = Theme.createStyledTextField("750000");
        txtHargaDasar.setText("750000");
        txtHargaDasar.setMaximumSize(new Dimension(310, 38));
        txtHargaDasar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new GridLayout(1, 3, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setMaximumSize(new Dimension(310, 40));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSimpan = Theme.createPrimaryButton("Simpan Data", Theme.NAVY_DARK, Color.WHITE);
        btnMaintenance = Theme.createPrimaryButton("Mulai Perawatan", Theme.DANGER_RED, Color.WHITE);
        JButton btnReset = Theme.createPrimaryButton("Reset", Theme.INPUT_BG, Theme.TEXT_DARK);
        btnSimpan.addActionListener(e -> saveRoom());
        btnMaintenance.addActionListener(e -> toggleMaintenance());
        btnReset.addActionListener(e -> clearForm());
        btnMaintenance.setEnabled(false);
        btnRow.add(btnSimpan);
        btnRow.add(btnMaintenance);
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
        formCard.add(Box.createVerticalStrut(18));
        formCard.add(btnRow);

        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(Color.WHITE);
        txtSearch = Theme.createStyledTextField("Cari nomor kamar atau tipe...");
        txtSearch.addActionListener(e -> filterRooms());
        JButton btnCari = Theme.createPrimaryButton("Cari", Theme.NAVY_DARK, Color.WHITE);
        btnCari.addActionListener(e -> filterRooms());
        JButton btnHapus = Theme.createPrimaryButton("Hapus Terpilih", Theme.DANGER_RED, Color.WHITE);
        btnHapus.addActionListener(e -> deleteSelectedRoom());
        toolbar.add(txtSearch, BorderLayout.CENTER);
        toolbar.add(btnCari, BorderLayout.EAST);

        JPanel tableHeader = new JPanel(new BorderLayout(8, 0));
        tableHeader.setBackground(Color.WHITE);
        tableHeader.add(toolbar, BorderLayout.CENTER);
        tableHeader.add(btnHapus, BorderLayout.EAST);

        String[] columns = {"NO. KAMAR", "LANTAI", "TIPE KAMAR", "HARGA PER MALAM", "STATUS KAMAR", "FASILITAS"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(tableModel);
        roomTable.setRowSorter(sorter);
        Theme.styleTable(roomTable);
        roomTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedRoom();
            }
        });

        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        add(formCard, BorderLayout.WEST);
        add(tableCard, BorderLayout.CENTER);

        try {
            repository.muat();
            refreshTable();
        } catch (HotelException ex) {
            showError(ex.getMessage());
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Room room : repository.tampil()) {
            tableModel.addRow(new Object[]{
                    room.getNomor(),
                    "Lantai " + room.getLantai(),
                    room.getTipe(),
                    String.format("Rp %,.0f", room.hitungHargaPerMalam()),
                    statusLabel(room.getStatus()),
                    room.getFasilitas()
            });
        }
        filterRooms();
    }

    private void saveRoom() {
        try {
            String number = txtNomorKamar.getText().trim();
            int floor = Integer.parseInt(((String) cmbLantai.getSelectedItem()).replace("Lantai ", ""));
            double basePrice = Double.parseDouble(txtHargaDasar.getText().trim());
            RoomStatus status = selectedRoomNumber == null
                    ? RoomStatus.AVAILABLE
                    : repository.cari(selectedRoomNumber).getStatus();
            Room room = createRoom(number, floor, basePrice, status, (String) cmbTipe.getSelectedItem());

            if (selectedRoomNumber == null) {
                repository.tambah(room);
            } else {
                repository.ubah(room);
            }
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Data kamar berhasil disimpan.");
        } catch (NumberFormatException ex) {
            showError("Lantai dan harga harus menggunakan format angka yang valid.");
        } catch (HotelException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private Room createRoom(String number, int floor, double basePrice, RoomStatus status, String type) {
        switch (type) {
            case "Deluxe":
                return new DeluxeRoom(number, floor, basePrice, status);
            case "Suite":
                return new SuiteRoom(number, floor, basePrice, status);
            default:
                return new StandardRoom(number, floor, basePrice, status);
        }
    }

    private void deleteSelectedRoom() {
        if (selectedRoomNumber == null) {
            showError("Pilih kamar yang akan dihapus terlebih dahulu.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Hapus kamar " + selectedRoomNumber + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Room room = repository.cari(selectedRoomNumber);
            if (room.getStatus() == RoomStatus.RESERVED || room.getStatus() == RoomStatus.OCCUPIED) {
                throw new IllegalStateException("Kamar dengan reservasi aktif tidak dapat dihapus.");
            }
            repository.hapus(selectedRoomNumber);
            refreshTable();
            clearForm();
        } catch (HotelException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void toggleMaintenance() {
        if (selectedRoomNumber == null) {
            showError("Pilih kamar terlebih dahulu.");
            return;
        }
        try {
            Room room = repository.cari(selectedRoomNumber);
            if (room.getStatus() == RoomStatus.AVAILABLE) {
                room.mulaiPerawatan();
            } else if (room.getStatus() == RoomStatus.MAINTENANCE) {
                room.selesaiPerawatan();
            } else {
                throw new IllegalStateException("Kamar yang dipesan atau terisi tidak dapat diubah ke status perawatan.");
            }
            repository.simpan();
            refreshTable();
            updateMaintenanceButton(room.getStatus());
        } catch (HotelException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void loadSelectedRoom() {
        int viewRow = roomTable.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int row = roomTable.convertRowIndexToModel(viewRow);
        selectedRoomNumber = tableModel.getValueAt(row, 0).toString();
        Room room = repository.cari(selectedRoomNumber);
        txtNomorKamar.setText(room.getNomor());
        txtNomorKamar.setEditable(false);
        cmbLantai.setSelectedItem("Lantai " + room.getLantai());
        cmbTipe.setSelectedItem(room.getTipe().substring(0, 1)
                + room.getTipe().substring(1).toLowerCase(java.util.Locale.ROOT));
        txtHargaDasar.setText(Double.toString(room.getHargaDasar()));
        updateMaintenanceButton(room.getStatus());
    }

    private void updateMaintenanceButton(RoomStatus status) {
        btnMaintenance.setEnabled(status == RoomStatus.AVAILABLE || status == RoomStatus.MAINTENANCE);
        btnMaintenance.setText(status == RoomStatus.MAINTENANCE ? "Selesai Perawatan" : "Mulai Perawatan");
    }

    private void filterRooms() {
        String query = txtSearch.getText().trim();
        sorter.setRowFilter(query.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
    }

    private String statusLabel(RoomStatus status) {
        switch (status) {
            case RESERVED:
                return "Dipesan";
            case OCCUPIED:
                return "Terisi";
            case MAINTENANCE:
                return "Perawatan";
            default:
                return "Tersedia";
        }
    }

    private void clearForm() {
        selectedRoomNumber = null;
        roomTable.clearSelection();
        txtNomorKamar.setText("");
        txtNomorKamar.setEditable(true);
        txtHargaDasar.setText("750000");
        cmbLantai.setSelectedIndex(0);
        cmbTipe.setSelectedIndex(0);
        updateMaintenanceButton(RoomStatus.AVAILABLE);
        btnMaintenance.setEnabled(false);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Manajemen Kamar", JOptionPane.ERROR_MESSAGE);
    }
}
