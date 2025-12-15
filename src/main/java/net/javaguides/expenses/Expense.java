package net.javaguides.expenses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Expense {
    private String id;
    private LocalDateTime dateTime;
    private String description;
    private String category;
    private BigDecimal amount;

    public Expense() {
        this.id = UUID.randomUUID().toString();
        this.dateTime = LocalDateTime.now();
    }

    public Expense(String id, LocalDateTime dateTime, String description, String category, BigDecimal amount) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.dateTime = dateTime == null ? LocalDateTime.now() : dateTime;
        this.description = description;
        this.category = category;
        this.amount = amount;
    }

    public static Expense of(String description, String category, BigDecimal amount) {
        return new Expense(null, LocalDateTime.now(), description, category, amount);
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Objects.equals(id, expense.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id='" + id + '\'' +
                ", dateTime=" + dateTime +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                '}';
    }

    // CSV format: id|dateTime|description|category|amount
    public String toCsvLine() {
        String desc = description == null ? "" : description.replace("|", " ").replace("\n", " ");
        String cat = category == null ? "" : category.replace("|", " ");
        String dt = dateTime == null ? "" : dateTime.toString();
        String amt = amount == null ? "0" : amount.toPlainString();
        return String.join("|", id, dt, desc, cat, amt);
    }

    public static Expense fromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|", -1);
        if (parts.length < 5) return null;
        String id = parts[0];
        LocalDateTime dt = null;
        try {
            dt = LocalDateTime.parse(parts[1]);
        } catch (Exception ignored) {
        }
        String desc = parts[2];
        String cat = parts[3];
        java.math.BigDecimal amt = java.math.BigDecimal.ZERO;
        try {
            amt = new java.math.BigDecimal(parts[4]);
        } catch (Exception ignored) {
        }
        return new Expense(id, dt, desc, cat, amt);
    }
}
