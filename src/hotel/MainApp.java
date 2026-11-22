package hotel;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import hotel.model.DeluxeRoom;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.model.StandardRoom;
import hotel.model.SuiteRoom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainApp {
    private static final List<Room> rooms = new ArrayList<>();
    private static final List<String> guestsJsonList = new ArrayList<>();
    private static final List<String> reservationsJsonList = new ArrayList<>();
    private static final List<String> paymentsJsonList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        isiDataAwal();
        isiTamuAwal();
        isiReservasiAwal();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/rooms", MainApp::handleRooms);
        server.createContext("/api/guests", MainApp::handleGuests);
        server.createContext("/api/reservations", MainApp::handleReservations);
        server.createContext("/api/payments", MainApp::handlePayments);

        server.setExecutor(null);
        server.start();
        System.out.println("Backend Java berjalan di http://localhost:8080");
    }

    private static void isiDataAwal() {
        rooms.add(new StandardRoom("101", 1, 300000, RoomStatus.OCCUPIED));
        rooms.add(new StandardRoom("102", 1, 300000, RoomStatus.AVAILABLE));
        rooms.add(new DeluxeRoom("201", 2, 300000, RoomStatus.AVAILABLE));
        rooms.add(new DeluxeRoom("202", 2, 300000, RoomStatus.RESERVED));
        rooms.add(new SuiteRoom("301", 3, 300000, RoomStatus.AVAILABLE));
    }

    private static void isiTamuAwal() {
        guestsJsonList.add("{\"guestId\":\"G001\",\"nama\":\"Budi Santoso\",\"nik\":\"3524010101900001\",\"noTelepon\":\"081234567801\",\"email\":\"budi@mail.com\"}");
        guestsJsonList.add("{\"guestId\":\"G002\",\"nama\":\"Siti Aminah\",\"nik\":\"3524010202920002\",\"noTelepon\":\"081234567802\",\"email\":\"siti@mail.com\"}");
    }

    private static void isiReservasiAwal() {
        reservationsJsonList.add("{\"reservationId\":\"R001\",\"guestId\":\"G001\",\"roomNumber\":\"101\",\"checkIn\":\"2026-11-21\",\"checkOut\":\"2026-11-23\",\"status\":\"CHECKED_IN\",\"totalBiaya\":600000}");
        reservationsJsonList.add("{\"reservationId\":\"R002\",\"guestId\":\"G002\",\"roomNumber\":\"202\",\"checkIn\":\"2026-11-22\",\"checkOut\":\"2026-11-24\",\"status\":\"BOOKED\",\"totalBiaya\":780000}");
    }

    private static void handleRooms(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            kirimResponse(exchange, 200, roomsToJson(rooms));
        } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = bacaBody(exchange.getRequestBody());
            String nomor = ambilNilaiString(body, "nomor").trim();
            int lantai = ambilNilaiInt(body, "lantai", 1);
            String tipe = ambilNilaiString(body, "tipe").trim();
            double hargaDasar = ambilNilaiDouble(body, "hargaDasar", 300000);

            if (nomor.isEmpty()) {
                kirimResponse(exchange, 400, "{\"error\":\"InvalidInputException: Nomor kamar tidak boleh kosong!\"}");
                return;
            }
            if (rooms.stream().anyMatch(r -> r.getNomor().equalsIgnoreCase(nomor))) {
                kirimResponse(exchange, 400, "{\"error\":\"HotelException: Nomor kamar sudah terdaftar!\"}");
                return;
            }

            Room kamarBaru;
            if ("DELUXE".equalsIgnoreCase(tipe)) {
                kamarBaru = new DeluxeRoom(nomor, lantai, hargaDasar, RoomStatus.AVAILABLE);
            } else if ("SUITE".equalsIgnoreCase(tipe)) {
                kamarBaru = new SuiteRoom(nomor, lantai, hargaDasar, RoomStatus.AVAILABLE);
            } else {
                kamarBaru = new StandardRoom(nomor, lantai, hargaDasar, RoomStatus.AVAILABLE);
            }

            rooms.add(kamarBaru);
            kirimResponse(exchange, 201, "{\"message\":\"Kamar berhasil ditambahkan\"}");
        } else {
            kirimResponse(exchange, 405, "{\"error\":\"Method tidak diizinkan\"}");
        }
    }

    private static void handleGuests(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            kirimResponse(exchange, 200, "[" + String.join(",", guestsJsonList) + "]");
        } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = bacaBody(exchange.getRequestBody());
            String nama = ambilNilaiString(body, "nama").trim();
            String nik = ambilNilaiString(body, "nik").trim();
            String noTelepon = ambilNilaiString(body, "noTelepon").trim();
            String email = ambilNilaiString(body, "email").trim();

            if (nama.isEmpty()) {
                kirimResponse(exchange, 400, "{\"error\":\"InvalidInputException: Nama tamu tidak boleh kosong!\"}");
                return;
            }
            if (!nik.matches("\\d{16}")) {
                kirimResponse(exchange, 400, "{\"error\":\"InvalidInputException: NIK harus 16 digit angka!\"}");
                return;
            }

            String nextId = String.format("G%03d", guestsJsonList.size() + 1);
            String newGuestJson = String.format(
                    "{\"guestId\":\"%s\",\"nama\":\"%s\",\"nik\":\"%s\",\"noTelepon\":\"%s\",\"email\":\"%s\"}",
                    escape(nextId), escape(nama), escape(nik), escape(noTelepon), escape(email)
            );
            guestsJsonList.add(newGuestJson);
            kirimResponse(exchange, 201, newGuestJson);
        } else {
            kirimResponse(exchange, 405, "{\"error\":\"Method tidak diizinkan\"}");
        }
    }

    private static void handleReservations(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            kirimResponse(exchange, 200, "[" + String.join(",", reservationsJsonList) + "]");
        } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = bacaBody(exchange.getRequestBody());
            String guestId = ambilNilaiString(body, "guestId").trim();
            String roomNumber = ambilNilaiString(body, "roomNumber").trim();
            String checkInStr = ambilNilaiString(body, "checkIn").trim();
            String checkOutStr = ambilNilaiString(body, "checkOut").trim();

            Room targetRoom = rooms.stream()
                    .filter(r -> r.getNomor().equals(roomNumber))
                    .findFirst()
                    .orElse(null);

            if (targetRoom == null) {
                kirimResponse(exchange, 404, "{\"error\":\"DataNotFoundException: Kamar tidak ditemukan!\"}");
                return;
            }
            if (!targetRoom.isAvailable()) {
                kirimResponse(exchange, 400, "{\"error\":\"RoomNotAvailableException: Kamar tidak tersedia untuk dipesan!\"}");
                return;
            }

            long malam;
            try {
                LocalDate d1 = LocalDate.parse(checkInStr);
                LocalDate d2 = LocalDate.parse(checkOutStr);
                malam = ChronoUnit.DAYS.between(d1, d2);
            } catch (Exception ex) {
                kirimResponse(exchange, 400, "{\"error\":\"InvalidInputException: Format tanggal tidak valid!\"}");
                return;
            }

            if (malam <= 0) {
                kirimResponse(exchange, 400, "{\"error\":\"InvalidInputException: Tanggal check-out harus setelah check-in!\"}");
                return;
            }

            // Ubah status kamar lewat method OOP pada Room
            targetRoom.pesan();

            double totalBiaya = targetRoom.hitungHargaPerMalam() * malam;
            String nextResId = String.format("R%03d", reservationsJsonList.size() + 1);
            String newResJson = String.format(
                    "{\"reservationId\":\"%s\",\"guestId\":\"%s\",\"roomNumber\":\"%s\",\"checkIn\":\"%s\",\"checkOut\":\"%s\",\"status\":\"BOOKED\",\"totalBiaya\":%.0f}",
                    escape(nextResId), escape(guestId), escape(roomNumber), escape(checkInStr), escape(checkOutStr), totalBiaya
            );
            reservationsJsonList.add(newResJson);
            kirimResponse(exchange, 201, newResJson);
        } else if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = bacaBody(exchange.getRequestBody());
            String reservationId = ambilNilaiString(body, "reservationId").trim();
            String aksi = ambilNilaiString(body, "action").trim().toUpperCase();

            int idx = -1;
            String currentJson = null;
            for (int i = 0; i < reservationsJsonList.size(); i++) {
                if (ambilNilaiString(reservationsJsonList.get(i), "reservationId").equals(reservationId)) {
                    idx = i;
                    currentJson = reservationsJsonList.get(i);
                    break;
                }
            }

            if (idx == -1 || currentJson == null) {
                kirimResponse(exchange, 404, "{\"error\":\"DataNotFoundException: Reservasi tidak ditemukan!\"}");
                return;
            }

            String roomNumber = ambilNilaiString(currentJson, "roomNumber");
            Room targetRoom = rooms.stream().filter(r -> r.getNomor().equals(roomNumber)).findFirst().orElse(null);

            String newStatus;
            if ("CHECK_IN".equals(aksi)) {
                if (targetRoom != null) targetRoom.checkIn();
                newStatus = "CHECKED_IN";
            } else if ("CHECK_OUT".equals(aksi)) {
                if (targetRoom != null) targetRoom.checkOut();
                newStatus = "CHECKED_OUT";
            } else {
                kirimResponse(exchange, 400, "{\"error\":\"InvalidInputException: Aksi reservasi tidak dikenali!\"}");
                return;
            }

            String updatedJson = currentJson.replaceFirst("\"status\":\"[^\"]*\"", "\"status\":\"" + newStatus + "\"");
            reservationsJsonList.set(idx, updatedJson);
            kirimResponse(exchange, 200, updatedJson);
        } else {
            kirimResponse(exchange, 405, "{\"error\":\"Method tidak diizinkan\"}");
        }
    }

    private static void handlePayments(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            kirimResponse(exchange, 200, "[" + String.join(",", paymentsJsonList) + "]");
        } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = bacaBody(exchange.getRequestBody());
            String reservationId = ambilNilaiString(body, "reservationId").trim();
            String method = ambilNilaiString(body, "method").trim();
            double amount = ambilNilaiDouble(body, "amount", 0);
            String date = ambilNilaiString(body, "date").trim();

            if (reservationId.isEmpty() || amount <= 0) {
                kirimResponse(exchange, 400, "{\"error\":\"InvalidInputException: Data pembayaran tidak valid!\"}");
                return;
            }

            String paymentId = String.format("P%03d", paymentsJsonList.size() + 1);
            String newPaymentJson = String.format(
                    "{\"paymentId\":\"%s\",\"reservationId\":\"%s\",\"amount\":%.0f,\"method\":\"%s\",\"date\":\"%s\"}",
                    escape(paymentId), escape(reservationId), amount, escape(method), escape(date)
            );
            paymentsJsonList.add(newPaymentJson);
            kirimResponse(exchange, 201, newPaymentJson);
        } else {
            kirimResponse(exchange, 405, "{\"error\":\"Method tidak diizinkan\"}");
        }
    }

    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String roomsToJson(List<Room> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Room r = list.get(i);
            sb.append("{")
              .append("\"nomor\":\"").append(escape(r.getNomor())).append("\",")
              .append("\"lantai\":").append(r.getLantai()).append(",")
              .append("\"tipe\":\"").append(escape(r.getTipe())).append("\",")
              .append("\"hargaDasar\":").append(r.getHargaDasar()).append(",")
              .append("\"hargaPerMalam\":").append(r.hitungHargaPerMalam()).append(",")
              .append("\"fasilitas\":\"").append(escape(r.getFasilitas())).append("\",")
              .append("\"status\":\"").append(escape(String.valueOf(r.getStatus()))).append("\"")
              .append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String bacaBody(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void kirimResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escape(String text) {
        return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String ambilNilaiString(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static int ambilNilaiInt(String json, String key, int def) {
        String pattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : def;
    }

    private static double ambilNilaiDouble(String json, String key, double def) {
        String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)";
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : def;
    }
}