package net.javaguides.expenses;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {
    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @GetMapping
    public List<Expense> all() {
        return service.listAll();
    }

    @PostMapping
    public ResponseEntity<Expense> add(@RequestBody Expense e) {
        Expense created = service.add(e);
        return ResponseEntity.created(URI.create("/api/expenses/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> edit(@PathVariable String id, @RequestBody Expense e) {
        boolean ok = service.edit(id, e);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean ok = service.delete(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
