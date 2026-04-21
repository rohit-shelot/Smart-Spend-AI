package com.smartspend.backend.dto;

import com.smartspend.backend.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TransactionDtos {
    @Data
    public static class TransactionRequest {
        @NotNull
        @DecimalMin("0.01")
        private BigDecimal amount;
        @NotNull
        private TransactionType type;
        @NotBlank
        private String category;
        private String description;
        @NotNull
        private LocalDate date;
    }

    @Data
    public static class TransactionResponse {
        private Long id;
        private BigDecimal amount;
        private TransactionType type;
        private String category;
        private String description;
        private LocalDate date;
    }

    @Data
    public static class DashboardSummary {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal balance;
        private Map<String, BigDecimal> expensesByCategory;
        private List<MonthlyPoint> monthlyTrend;
    }

    @Data
    public static class MonthlyPoint {
        private String month;
        private BigDecimal income;
        private BigDecimal expense;
    }
}
