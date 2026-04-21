package com.smartspend.backend.service;

import com.smartspend.backend.dto.InsightDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InsightService {
    @Value("${app.groq.api-key}")
    private String apiKey;

    @Value("${app.groq.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public InsightDtos.InsightResponse analyze(InsightDtos.InsightRequest request) {
        String fallback = fallbackInsight(request);
        if (apiKey == null || apiKey.isBlank()) {
            return new InsightDtos.InsightResponse(fallback, score(request));
        }
        try {
            String prompt = "Analyze this finance data and provide concise, actionable suggestions in under 80 words: " + request;
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a personal finance assistant."),
                    Map.of("role", "user", "content", prompt)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map response = restTemplate.postForObject("https://api.groq.com/openai/v1/chat/completions", new HttpEntity<>(body, headers), Map.class);
            String content = fallback;
            if (response != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.get("content") != null) {
                        content = message.get("content").toString();
                    }
                }
            }
            return new InsightDtos.InsightResponse(content, score(request));
        } catch (Exception ignored) {
            return new InsightDtos.InsightResponse(fallback, score(request));
        }
    }

    private int score(InsightDtos.InsightRequest request) {
        BigDecimal expenses = request.getExpenses().stream().map(InsightDtos.CategoryExpense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (request.getIncome().compareTo(BigDecimal.ZERO) <= 0) return 20;
        BigDecimal ratio = expenses.divide(request.getIncome(), 4, java.math.RoundingMode.HALF_UP);
        int value = (int) Math.max(10, Math.min(100, 100 - ratio.multiply(BigDecimal.valueOf(100)).intValue()));
        return value;
    }

    private String fallbackInsight(InsightDtos.InsightRequest request) {
        BigDecimal expenses = request.getExpenses().stream().map(InsightDtos.CategoryExpense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = request.getIncome().subtract(expenses);
        return "Your total expense is " + expenses + ". Remaining balance is " + balance + ". Reduce high-spend categories to improve monthly savings.";
    }
}
