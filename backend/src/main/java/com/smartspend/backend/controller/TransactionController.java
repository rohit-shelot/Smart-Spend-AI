package com.smartspend.backend.controller;

import com.smartspend.backend.dto.TransactionDtos;
import com.smartspend.backend.entity.TransactionType;
import com.smartspend.backend.entity.User;
import com.smartspend.backend.service.AuthService;
import com.smartspend.backend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final AuthService authService;

    public TransactionController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    @GetMapping
    public List<TransactionDtos.TransactionResponse> all(
            Authentication authentication,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TransactionType type) {
        User user = authService.currentUser(authentication.getName());
        return transactionService.list(user, month, year, category, type);
    }

    @PostMapping
    public TransactionDtos.TransactionResponse create(Authentication authentication, @Valid @RequestBody TransactionDtos.TransactionRequest request) {
        User user = authService.currentUser(authentication.getName());
        return transactionService.add(user, request);
    }

    @PutMapping("/{id}")
    public TransactionDtos.TransactionResponse update(Authentication authentication, @PathVariable Long id, @Valid @RequestBody TransactionDtos.TransactionRequest request) {
        User user = authService.currentUser(authentication.getName());
        return transactionService.update(user, id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(Authentication authentication, @PathVariable Long id) {
        User user = authService.currentUser(authentication.getName());
        transactionService.delete(user, id);
    }

    @GetMapping("/summary")
    public TransactionDtos.DashboardSummary summary(Authentication authentication) {
        User user = authService.currentUser(authentication.getName());
        return transactionService.summary(user);
    }
}
