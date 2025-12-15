package net.javaguides.expenses;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Path storage = Path.of("expenses.csv");
        ExpenseManager manager = new ExpenseManager(storage);

        System.out.println("Initial entries: ");
        manager.listExpenses().forEach(System.out::println);

        System.out.println("\nAdding sample expenses...");
        manager.addExpense("Coffee", "Food", new BigDecimal("3.50"));
        manager.addExpense("Groceries", "Food", new BigDecimal("54.20"));
        manager.addExpense("New headphones", "Hobby", new BigDecimal("79.99"));
        manager.addExpense("Electric bill", "House", new BigDecimal("120.00"));

        System.out.println("\nAll expenses (in entry order):");
        manager.listSortedByEntryOrder().forEach(System.out::println);

        System.out.println("\nTotal spent: " + manager.totalSpent());

        System.out.println("\nTotals by category:");
        for (Map.Entry<String, BigDecimal> en : manager.totalsByCategory().entrySet()) {
            System.out.println(" - " + en.getKey() + ": " + en.getValue());
        }

        System.out.println("\nYou can edit or delete entries by using ExpenseManager methods programmatically or wiring this to a REST API / UI.");
    }
}
