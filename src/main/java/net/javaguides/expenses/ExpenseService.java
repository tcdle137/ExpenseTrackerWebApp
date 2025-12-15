package net.javaguides.expenses;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ExpenseService {
    private final ExpenseManager manager;

    public ExpenseService() {
        this.manager = new ExpenseManager(Path.of("expenses.csv"));
    }

    public List<Expense> listAll() {
        return manager.listExpenses();
    }

    public Expense add(Expense e) {
        return manager.addExpense(e.getDescription(), e.getCategory(), e.getAmount());
    }

    public boolean edit(String id, Expense e) {
        return manager.editExpense(id, e.getDescription(), e.getCategory(), e.getAmount());
    }

    public boolean delete(String id) {
        return manager.deleteExpense(id);
    }

    public Map<String, java.math.BigDecimal> totalsByCategory() {
        return manager.totalsByCategory();
    }
}
