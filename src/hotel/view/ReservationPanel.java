package com.grandluxury.view;

import com.grandluxury.model.Reservation;
import com.grandluxury.service.ReservationService;
import hotel.util.HotelException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.NoSuchElementException;

public class ReservationPanel extends JPanel {
    private final ReservationService service;
    private final JTextField txtGuestId = new JTextField();
    private final JTextField txtRoomNumber = new JTextField();
    private final JTextField txtCheckIn = new JTextField();
    private final JTextField txtCheckOut = new JTextField();
    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID RESERVASI", "ID TAMU", "KAMAR", "CHECK-IN", "CHECK-OUT", "STATUS", "TOTAL"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable reservationTable = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private String selectedReservationId;

    public ReservationPanel() {
        this(null);
    }

    public ReservationPanel(ReservationService service) {
        this.service = service;
        if (service == null) {
            buildPrototypePanel();
        } else {
            buildServicePanel();
        }
    }

    private void buildServicePanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = Theme.createCardPanel();
        form.setLayout(new BorderLayout(0, 10));
        form.add(new JLabel("<html><b style='font-size:15px;'>Kelola Reservasi</b><br>"
                + "<span style='color:#64748B;font-size:11px;'>Tanggal menggunakan format YYYY-MM-DD.</span></html>"),
                BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(1, 4, 10, 0));
        fields.setBackground(Color.WHITE);
        txtGuestId.setText("");
        txtRoomNumber.setText("");
        txtCheckIn.setText(LocalDate.now().toString());
        txtCheckOut.setText(LocalDate.now().plusDays(1).toString());
        fields.add(wrapField("ID TAMU", txtGuestId));
        fields.add(wrapField("NOMOR KAMAR", txtRoomNumber));
        fields.add(wrapField("CHECK-IN", txtCheckIn));
        fields.add(wrapField("CHECK-OUT", txtCheckOut));
        form.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setBackground(Color.WHITE);
        JButton btnCreate = Theme.createPrimaryButton("Buat Reservasi", Theme.CYAN_ACCENT, Color.WHITE);
        JButton btnUpdate = Theme.createPrimaryButton("Ubah", Theme.NAVY_DARK, Color.WHITE);
        JButton btnCancel = Theme.createPrimaryButton("Batalkan", Theme.DANGER_RED, Color.WHITE);
        JButton btnCheckIn = Theme.createPrimaryButton("Check-In", Theme.NAVY_DARK, Color.WHITE);
        JButton btnCheckOut = Theme.createPrimaryButton("Check-Out", Theme.TEAL_BADGE, Color.WHITE);
        JButton btnCheckAvailability = Theme.createPrimaryButton("Cek Kamar", Theme.CYAN_ACCENT, Color.WHITE);
        btnCreate.addActionListener(e -> runAction(() -> service.createReservation(
                txtGuestId.getText(), txtRoomNumber.getText(), parseCheckIn(), parseCheckOut()
        )));
        btnUpdate.addActionListener(e -> runAction(() -> service.updateReservation(
                requireSelectedReservation(), txtRoomNumber.getText(), parseCheckIn(), parseCheckOut()
        )));
        btnCancel.addActionListener(e -> runAction(() -> service.cancelReservation(requireSelectedReservation())));
        btnCheckIn.addActionListener(e -> runAction(() -> service.checkIn(requireSelectedReservation())));
        btnCheckOut.addActionListener(e -> runAction(() -> service.checkOut(requireSelectedReservation())));
        btnCheckAvailability.addActionListener(e -> checkAvailableRooms());
        actions.add(btnCreate);
        actions.add(btnUpdate);
        actions.add(btnCancel);
        actions.add(btnCheckIn);
        actions.add(btnCheckOut);
        actions.add(btnCheckAvailability);
        form.add(actions, BorderLayout.SOUTH);

        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(Color.WHITE);
        txtSearch.setToolTipText("Cari ID reservasi, ID tamu, atau nomor kamar");
        JButton btnSearch = Theme.createPrimaryButton("Cari", Theme.NAVY_DARK, Color.WHITE);
        btnSearch.addActionListener(e -> filterReservations());
        txtSearch.addActionListener(e -> filterReservations());
        toolbar.add(txtSearch, BorderLayout.CENTER);
        toolbar.add(btnSearch, BorderLayout.EAST);

        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationTable.setRowSorter(sorter);
        Theme.styleTable(reservationTable);
        reservationTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedReservation();
            }
        });
        tableCard.add(toolbar, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(reservationTable), BorderLayout.CENTER);

        add(form, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        refreshReservations();
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

    private LocalDate parseCheckIn() {
        return LocalDate.parse(txtCheckIn.getText().trim());
    }

    private LocalDate parseCheckOut() {
        return LocalDate.parse(txtCheckOut.getText().trim());
    }

    private String requireSelectedReservation() {
        if (selectedReservationId == null) {
            throw new IllegalStateException("Pilih reservasi pada tabel terlebih dahulu.");
        }
        return selectedReservationId;
    }

    private void runAction(ReservationAction action) {
        try {
            action.run();
            refreshReservations();
            JOptionPane.showMessageDialog(this, "Aksi reservasi berhasil diproses.");
        } catch (IOException | IllegalArgumentException | IllegalStateException
                 | NoSuchElementException | DateTimeException | HotelException ex) {
            showError(ex.getMessage());
        }
    }

    private void checkAvailableRooms() {
        try {
            java.util.List<String> rooms = service.findAvailableRooms(parseCheckIn(), parseCheckOut());
            if (rooms.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada kamar tersedia pada rentang tanggal tersebut.");
                return;
            }
            String availableRooms = String.join(", ", rooms);
            txtRoomNumber.setText(rooms.get(0));
            JOptionPane.showMessageDialog(
                    this,
                    "Kamar tersedia: " + availableRooms + "\nNomor kamar pertama sudah dipilih pada form."
            );
        } catch (IOException | IllegalArgumentException | DateTimeException | HotelException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshReservations() {
        tableModel.setRowCount(0);
        for (Reservation reservation : service.getReservations()) {
            tableModel.addRow(new Object[]{
                    reservation.getId(),
                    reservation.getGuestId(),
                    reservation.getRoomNumber(),
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate(),
                    reservation.getStatus(),
                    formatCurrency(reservation.getTotalCost())
            });
        }
        filterReservations();
    }

    private void filterReservations() {
        String query = txtSearch.getText().trim();
        sorter.setRowFilter(query.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
    }

    private void loadSelectedReservation() {
        int viewRow = reservationTable.getSelectedRow();
        if (viewRow < 0) {
            selectedReservationId = null;
            return;
        }
        int row = reservationTable.convertRowIndexToModel(viewRow);
        selectedReservationId = tableModel.getValueAt(row, 0).toString();
        txtGuestId.setText(tableModel.getValueAt(row, 1).toString());
        txtRoomNumber.setText(tableModel.getValueAt(row, 2).toString());
        txtCheckIn.setText(tableModel.getValueAt(row, 3).toString());
        txtCheckOut.setText(tableModel.getValueAt(row, 4).toString());
    }

    private String formatCurrency(BigDecimal amount) {
        return "Rp " + String.format("%,.0f", amount);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Reservasi", JOptionPane.ERROR_MESSAGE);
    }

    private void buildPrototypePanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_LIGHT);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new GridLayout(1, 4, 14, 0));
        JTextField checkIn = Theme.createStyledTextField("YYYY-MM-DD");
        checkIn.setText(LocalDate.now().toString());
        JTextField checkOut = Theme.createStyledTextField("YYYY-MM-DD");
        checkOut.setText(LocalDate.now().plusDays(1).toString());
        JSpinner nights = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        JButton checkButton = Theme.createPrimaryButton("Cek Ketersediaan", Theme.CYAN_ACCENT, Color.WHITE);
        checkButton.addActionListener(e -> JOptionPane.showMessageDialog(
                this, "Hubungkan panel ke ReservationService untuk memeriksa ketersediaan."
        ));
        filterCard.add(createBox("TANGGAL CHECK-IN", checkIn));
        filterCard.add(createBox("TANGGAL CHECK-OUT", checkOut));
        filterCard.add(createBox("DURASI (MALAM)", nights));
        filterCard.add(createBox("PENCARIAN", checkButton));

        JPanel notice = Theme.createCardPanel();
        notice.setLayout(new BorderLayout());
        notice.add(new JLabel("<html><b>Integrasi reservasi belum dikonfigurasi.</b><br>"
                + "Panel ini perlu dibuat dengan ReservationService dan RoomGateway dari modul kamar.</html>"),
                BorderLayout.CENTER);
        add(filterCard, BorderLayout.NORTH);
        add(notice, BorderLayout.CENTER);
    }

    private JPanel createBox(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(Theme.TEXT_MUTED);
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    @FunctionalInterface
    private interface ReservationAction {
        void run() throws IOException;
    }
}
