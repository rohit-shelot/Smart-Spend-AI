package com.smartspend.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class InsightDtos {
    @Data
    public static class CategoryExpense {
        private String category;
        private BigDecimal amount;
    }

    @Data
    public static class InsightRequest {
        private BigDecimal income;
        private List<CategoryExpense> expenses;
    }

    @Data
    public static class InsightResponse {
        private final String insight;
        private final int financialHealthScore;
    }
}
