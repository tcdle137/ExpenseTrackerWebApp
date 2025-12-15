package net.javaguides.expenses;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExpenseRepository {
    private final Path file;

    public ExpenseRepository(Path file) {
        this.file = Objects.requireNonNull(file);
        try {
            ensureFile();
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize repository file", e);
        }
    }

    private void ensureFile() throws IOException {
        if (Files.notExists(file)) {
            if (file.getParent() != null && Files.notExists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }
            Files.write(file, List.of("id|dateTime|description|category|amount"), StandardCharsets.UTF_8);
        }
    }

    public List<Expense> loadAll() {
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                    .skip(1)
                    .map(Expense::fromCsvLine)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read expenses file", e);
        }
    }

    public void saveAll(List<Expense> all) {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id|dateTime|description|category|amount");
            for (Expense e : all) {
                lines.add(e.toCsvLine());
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write expenses file", e);
        }
    }

    public void append(Expense expense) {
        try {
            String line = expense.toCsvLine() + System.lineSeparator();
            Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to append expense", e);
        }
    }
}
