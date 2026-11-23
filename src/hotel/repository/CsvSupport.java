package hotel.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

final class CsvSupport {
    private CsvSupport() {
    }

    static List<List<String>> read(Path path, int expectedColumns) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        if (!Files.exists(path)) {
            return rows;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isEmpty()) {
                    continue;
                }
                List<String> row;
                try {
                    row = parseLine(line);
                } catch (IllegalArgumentException ex) {
                    throw new IOException("Format CSV tidak valid pada " + path + ", baris " + lineNumber + ".", ex);
                }
                if (row.size() != expectedColumns) {
                    throw new IOException("Jumlah kolom CSV tidak valid pada " + path + ", baris " + lineNumber + ".");
                }
                rows.add(row);
            }
        }
        return rows;
    }

    static void write(Path path, List<List<String>> rows) throws IOException {
        Path absolutePath = path.toAbsolutePath();
        Path parent = absolutePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path tempFile = Files.createTempFile(parent, absolutePath.getFileName().toString(), ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                for (List<String> row : rows) {
                    for (int i = 0; i < row.size(); i++) {
                        if (i > 0) {
                            writer.write(',');
                        }
                        writer.write(escape(row.get(i)));
                    }
                    writer.newLine();
                }
            }
            try {
                Files.move(tempFile, absolutePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tempFile, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static String escape(String value) {
        String normalized = value == null ? "" : value;
        if (normalized.indexOf(',') < 0 && normalized.indexOf('"') < 0) {
            return normalized;
        }
        return '"' + normalized.replace("\"", "\"\"") + '"';
    }

    private static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (quoted) {
                if (current == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        closedQuote = true;
                    }
                } else {
                    field.append(current);
                }
            } else if (current == ',') {
                fields.add(field.toString());
                field.setLength(0);
                closedQuote = false;
            } else if (current == '"') {
                if (field.length() == 0 && !closedQuote) {
                    quoted = true;
                } else {
                    throw new IllegalArgumentException("Tanda kutip CSV berada di posisi yang tidak valid.");
                }
            } else if (closedQuote) {
                throw new IllegalArgumentException("Karakter setelah tanda kutip CSV.");
            } else {
                field.append(current);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException("Tanda kutip CSV tidak berpasangan.");
        }
        fields.add(field.toString());
        return fields;
    }
}
