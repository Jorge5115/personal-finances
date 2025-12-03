package com.example.finances_backend.controller;

import com.example.finances_backend.model.Transaction;
import com.example.finances_backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    // GET all transactions
    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // GET transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        return transaction.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // CREATE new transaction
    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    // UPDATE transaction
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction transactionDetails) {
        Optional<Transaction> optionalTransaction = transactionRepository.findById(id);

        if (optionalTransaction.isPresent()) {
            Transaction transaction = optionalTransaction.get();
            transaction.setDescription(transactionDetails.getDescription());
            transaction.setAmount(transactionDetails.getAmount());
            transaction.setType(transactionDetails.getType());
            transaction.setCategory(transactionDetails.getCategory());
            transaction.setDate(transactionDetails.getDate());

            Transaction updatedTransaction = transactionRepository.save(transaction);
            return ResponseEntity.ok(updatedTransaction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET transactions by type
    @GetMapping("/type/{type}")
    public List<Transaction> getTransactionsByType(@PathVariable String type) {
        return transactionRepository.findByType(type.toUpperCase());
    }

    // GET dashboard statistics
    @GetMapping("/statistics/summary")
    public ResponseEntity<?> getDashboardSummary() {
        Double totalIncome = transactionRepository.sumTotalIncome() != null ? transactionRepository.sumTotalIncome() : 0.0;
        Double totalExpenses = transactionRepository.sumTotalExpenses() != null ? transactionRepository.sumTotalExpenses() : 0.0;
        Double balance = totalIncome + totalExpenses; // Expenses are negative, income positive

        return ResponseEntity.ok().body(new DashboardSummary(totalIncome, Math.abs(totalExpenses), balance));
    }

    // Static inner class for dashboard response
    private static class DashboardSummary {
        public Double totalIncome;
        public Double totalExpenses;
        public Double balance;

        public DashboardSummary(Double totalIncome, Double totalExpenses, Double balance) {
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
            this.balance = balance;
        }
    }
}