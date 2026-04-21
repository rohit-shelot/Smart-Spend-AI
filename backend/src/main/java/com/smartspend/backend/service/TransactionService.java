package com.smartspend.backend.service;

import com.smartspend.backend.dto.TransactionDtos;
import com.smartspend.backend.entity.Transaction;
import com.smartspend.backend.entity.TransactionType;
import com.smartspend.backend.entity.User;
import com.smartspend.backend.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionDtos.TransactionResponse add(User user, TransactionDtos.TransactionRequest request) {
        Transaction transaction = new Transaction();
        apply(transaction, request, user);
        return map(transactionRepository.save(transaction));
    }

    public List<TransactionDtos.TransactionResponse> list(User user, Integer month, Integer year, String category, TransactionType type) {
        return transactionRepository.findByUserOrderByDateDesc(user).stream()
                .filter(t -> month == null || t.getDate().getMonthValue() == month)
                .filter(t -> year == null || t.getDate().getYear() == year)
                .filter(t -> category == null || category.isBlank() || t.getCategory().equalsIgnoreCase(category))
                .filter(t -> type == null || t.getType() == type)
                .map(this::map)
                .toList();
    }

    public TransactionDtos.TransactionResponse update(User user, Long id, TransactionDtos.TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        apply(transaction, request, user);
        return map(transactionRepository.save(transaction));
    }

    public void delete(User user, Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        transactionRepository.delete(transaction);
    }

    public TransactionDtos.DashboardSummary summary(User user) {
        List<Transaction> all = transactionRepository.findByUserOrderByDateDesc(user);
        BigDecimal totalIncome = all.stream().filter(t -> t.getType() == TransactionType.INCOME).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = all.stream().filter(t -> t.getType() == TransactionType.EXPENSE).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryTotals = all.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        Map<YearMonth, List<Transaction>> byMonth = all.stream().collect(Collectors.groupingBy(t -> YearMonth.from(t.getDate())));
        List<TransactionDtos.MonthlyPoint> trend = byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(e -> {
                    BigDecimal income = e.getValue().stream().filter(t -> t.getType() == TransactionType.INCOME).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal expense = e.getValue().stream().filter(t -> t.getType() == TransactionType.EXPENSE).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    TransactionDtos.MonthlyPoint point = new TransactionDtos.MonthlyPoint();
                    point.setMonth(e.getKey().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + e.getKey().getYear());
                    point.setIncome(income);
                    point.setExpense(expense);
                    return point;
                }).toList();

        TransactionDtos.DashboardSummary summary = new TransactionDtos.DashboardSummary();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setBalance(totalIncome.subtract(totalExpense));
        summary.setExpensesByCategory(new LinkedHashMap<>(categoryTotals));
        summary.setMonthlyTrend(trend);
        return summary;
    }

    private void apply(Transaction transaction, TransactionDtos.TransactionRequest request, User user) {
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setUser(user);
    }

    private TransactionDtos.TransactionResponse map(Transaction t) {
        TransactionDtos.TransactionResponse response = new TransactionDtos.TransactionResponse();
        response.setId(t.getId());
        response.setAmount(t.getAmount());
        response.setType(t.getType());
        response.setCategory(t.getCategory());
        response.setDescription(t.getDescription());
        response.setDate(t.getDate());
        return response;
    }
}
