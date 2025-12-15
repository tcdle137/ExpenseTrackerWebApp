package net.javaguides.expenses;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {
    private final ExpenseManager manager;

    public ExpenseService() {
        // store CSV in working directory; adjust path if you want a different location
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
