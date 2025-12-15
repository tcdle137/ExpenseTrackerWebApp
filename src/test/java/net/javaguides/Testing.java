package net.javaguides;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.javaguides.expenses.Expense;
import net.javaguides.expenses.ExpenseManager;

@DisplayName("ExpenseManager integration tests")
public class Testing {

	private Path tempFile;

	private ExpenseManager createManager() throws Exception {
		// create and then delete to allow repository to write header on initialization
		tempFile = Files.createTempFile("expenses-test", ".csv");
		Files.deleteIfExists(tempFile);
		return new ExpenseManager(tempFile);
	}

	@AfterEach
	public void cleanup() throws Exception {
		if (tempFile != null && Files.exists(tempFile)) {
			Files.delete(tempFile);
		}
	}

	@Test
	@DisplayName("add and list expenses")
	public void testAddAndList() throws Exception {
		ExpenseManager manager = createManager();

		manager.addExpense("Coffee", "Food", new BigDecimal("3.50"));
		manager.addExpense("Groceries", "Food", new BigDecimal("20.00"));
		manager.addExpense("Book", "Hobby", new BigDecimal("12.99"));

		List<Expense> all = manager.listExpenses();
		assertEquals(3, all.size(), "Should have 3 expenses after adding");
	}

	@Test
	@DisplayName("calculate total spent")
	public void testTotalSpent() throws Exception {
		ExpenseManager manager = createManager();
		manager.addExpense("A", "X", new BigDecimal("1.25"));
		manager.addExpense("B", "Y", new BigDecimal("2.75"));
		assertEquals(new BigDecimal("4.00"), manager.totalSpent());
	}

	@Test
	@DisplayName("totals by category")
	public void testTotalsByCategory() throws Exception {
		ExpenseManager manager = createManager();
		manager.addExpense("Coffee", "Food", new BigDecimal("3.50"));
		manager.addExpense("Groceries", "Food", new BigDecimal("20.00"));
		manager.addExpense("Book", "Hobby", new BigDecimal("12.99"));
		Map<String, BigDecimal> byCat = manager.totalsByCategory();
		assertEquals(new BigDecimal("23.50"), byCat.get("Food"));
		assertEquals(new BigDecimal("12.99"), byCat.get("Hobby"));
	}

	@Test
	@DisplayName("persistence reloads data from file")
	public void testPersistenceReload() throws Exception {
		ExpenseManager manager = createManager();
		manager.addExpense("Coffee", "Food", new BigDecimal("3.50"));
		manager.addExpense("Groceries", "Food", new BigDecimal("20.00"));
		ExpenseManager manager2 = new ExpenseManager(tempFile);
		List<Expense> all2 = manager2.listExpenses();
		assertEquals(2, all2.size(), "Reloaded manager should see 2 expenses");
	}

	@Test
	@DisplayName("edit and delete expense operations")
	public void testEditAndDelete() throws Exception {
		ExpenseManager manager = createManager();

		Expense e = manager.addExpense("Lunch", "Food", new BigDecimal("8.00"));
		String id = e.getId();

		boolean edited = manager.editExpense(id, "Lunch with friend", "Food", new BigDecimal("10.00"));
		assertTrue(edited, "editExpense should return true for existing id");

		Expense updated = manager.findById(id).orElseThrow();
		assertEquals("Lunch with friend", updated.getDescription());
		assertEquals(new BigDecimal("10.00"), updated.getAmount());

		boolean deleted = manager.deleteExpense(id);
		assertTrue(deleted, "deleteExpense should remove the entry");
		assertTrue(manager.listExpenses().isEmpty(), "Manager should have no expenses after delete");
	}
}