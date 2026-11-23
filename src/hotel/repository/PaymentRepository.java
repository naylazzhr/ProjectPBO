package hotel.repository;

import hotel.model.Payment;
import hotel.model.PaymentMethod;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public class PaymentRepository implements Repository<Payment, String> {
    private final Path file;
    private final Map<String, Payment> payments = new LinkedHashMap<>();

    public PaymentRepository() {
        this(Paths.get("data", "payments.csv"));
    }

    public PaymentRepository(Path file) {
        this.file = Objects.requireNonNull(file);
    }

    private String getKey(Payment payment) {
        return payment.getPaymentId();
    }

    @Override
    public void add(Payment payment) throws IOException {
        String id = getKey(payment);
        if (payments.containsKey(id)) {
            throw new IllegalArgumentException("ID pembayaran sudah terdaftar: " + id);
        }
        Map<String, Payment> updated = new LinkedHashMap<>(payments);
        updated.put(id, payment);
        persist(updated);
        payments.put(id, payment);
    }

    @Override
    public void update(Payment payment) throws IOException {
        String id = getKey(payment);
        if (!payments.containsKey(id)) {
            throw new NoSuchElementException("Pembayaran tidak ditemukan: " + id);
        }
        Map<String, Payment> updated = new LinkedHashMap<>(payments);
        updated.put(id, payment);
        persist(updated);
        payments.put(id, payment);
    }

    @Override
    public void delete(String id) throws IOException {
        if (!payments.containsKey(id)) {
            throw new NoSuchElementException("Pembayaran tidak ditemukan: " + id);
        }
        Map<String, Payment> updated = new LinkedHashMap<>(payments);
        updated.remove(id);
        persist(updated);
        payments.remove(id);
    }

    @Override
    public Optional<Payment> findById(String id) {
        return Optional.ofNullable(payments.get(id));
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(payments.values());
    }

    @Override
    public void save() throws IOException {
        persist(payments);
    }

    @Override
    public void load() throws IOException {
        Map<String, Payment> loaded = new LinkedHashMap<>();
        for (List<String> row : CsvSupport.read(file, 6)) {
            Payment payment;
            try {
                payment = new Payment(
                        row.get(0),
                        row.get(1),
                        Double.parseDouble(row.get(2)),
                        PaymentMethod.valueOf(row.get(3)),
                        row.get(5)
                );
                payment.setPaymentDate(LocalDateTime.parse(row.get(4)));
            } catch (IllegalArgumentException | java.time.DateTimeException ex) {
                throw new IOException("Data pembayaran tidak valid di " + file + ".", ex);
            }
            String id = getKey(payment);
            if (loaded.putIfAbsent(id, payment) != null) {
                throw new IOException("ID pembayaran duplikat di " + file + ": " + id);
            }
        }
        payments.clear();
        payments.putAll(loaded);
    }

    private void persist(Map<String, Payment> source) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        for (Payment payment : source.values()) {
            rows.add(List.of(
                    payment.getPaymentId(),
                    payment.getReservationId(),
                    String.valueOf(payment.getAmount()),
                    payment.getPaymentMethod().name(),
                    payment.getPaymentDate().toString(),
                    payment.getStatus()
            ));
        }
        CsvSupport.write(file, rows);
    }
}