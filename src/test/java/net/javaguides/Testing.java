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
	@DisplayName("graceful handling of malformed file lines")
	public void testMalformedFileRead() throws Exception {
		// create a file with header + malformed line
		tempFile = Files.createTempFile("expenses-test-malformed", ".csv");
		List<String> lines = List.of("id|dateTime|description|category|amount", "bad|line|missing|fields");
		Files.write(tempFile, lines);
		ExpenseManager manager = new ExpenseManager(tempFile);
		// malformed line should be ignored and no exception thrown; list should be empty
		List<Expense> all = manager.listExpenses();
		assertEquals(0, all.size(), "Malformed lines should be ignored by loader");
	}

	@Test
	@DisplayName("invalid numeric input in file results in zero amount and does not crash")
	public void testInvalidAmountInFile() throws Exception {
		tempFile = Files.createTempFile("expenses-test-invalid-amount", ".csv");
		List<String> lines = List.of(
				"id|dateTime|description|category|amount",
				"1|2025-01-01T00:00:00|BadAmount|Food|not-a-number"
		);
		Files.write(tempFile, lines);
		ExpenseManager manager = new ExpenseManager(tempFile);
		List<Expense> all = manager.listExpenses();
		assertEquals(1, all.size(), "Line with invalid amount should still produce an Expense with amount 0");
		assertEquals(new BigDecimal("0"), all.get(0).getAmount());
		assertEquals(new BigDecimal("0"), manager.totalSpent());
	}

	@Test
	@DisplayName("math logic correctness for totals")
	public void testMathLogicTotals() throws Exception {
		ExpenseManager manager = createManager();
		manager.addExpense("A", "X", new BigDecimal("0.10"));
		manager.addExpense("B", "X", new BigDecimal("0.20"));
		// using BigDecimal with string ensures exact decimal arithmetic
		assertEquals(new BigDecimal("0.30"), manager.totalSpent());
	}

	@Test
	@DisplayName("display / ordering: entries maintain insertion order")
	public void testDisplayOrder() throws Exception {
		ExpenseManager manager = createManager();
		Expense e1 = manager.addExpense("First", "Misc", new BigDecimal("1.00"));
		Expense e2 = manager.addExpense("Second", "Misc", new BigDecimal("2.00"));
		List<Expense> ordered = manager.listSortedByEntryOrder();
		assertEquals(2, ordered.size());
		assertEquals(e1.getId(), ordered.get(0).getId());
		assertEquals(e2.getId(), ordered.get(1).getId());
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