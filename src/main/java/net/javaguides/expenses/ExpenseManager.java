package net.javaguides.expenses;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExpenseManager {
    private final ExpenseRepository repository;
    private final List<Expense> expenses = new ArrayList<>();

    public ExpenseManager(Path storageFile) {
        this.repository = new ExpenseRepository(storageFile);
        load();
    }

    public void load() {
        expenses.clear();
        expenses.addAll(repository.loadAll());
    }

    public Expense addExpense(String description, String category, BigDecimal amount) {
        Expense e = Expense.of(description, category, amount);
        expenses.add(e);
        repository.append(e);
        return e;
    }

    public List<Expense> listExpenses() {
        return new ArrayList<>(expenses);
    }

    public BigDecimal totalSpent() {
        return expenses.stream()
                .map(Expense::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> totalsByCategory() {
        Map<String, BigDecimal> map = new HashMap<>();
        for (Expense e : expenses) {
            String cat = e.getCategory() == null ? "Uncategorized" : e.getCategory();
            BigDecimal amt = e.getAmount() == null ? BigDecimal.ZERO : e.getAmount();
            map.put(cat, map.getOrDefault(cat, BigDecimal.ZERO).add(amt));
        }
        return map;
    }

    public Optional<Expense> findById(String id) {
        return expenses.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public boolean editExpense(String id, String newDescription, String newCategory, BigDecimal newAmount) {
        Optional<Expense> found = findById(id);
        if (found.isEmpty()) return false;
        Expense e = found.get();
        e.setDescription(newDescription);
        e.setCategory(newCategory);
        e.setAmount(newAmount);
        repository.saveAll(expenses);
        return true;
    }

    public boolean deleteExpense(String id) {
        boolean removed = expenses.removeIf(e -> e.getId().equals(id));
        if (removed) repository.saveAll(expenses);
        return removed;
    }

    public List<Expense> listSortedByEntryOrder() {
        return expenses.stream().collect(Collectors.toList());
    }
}
