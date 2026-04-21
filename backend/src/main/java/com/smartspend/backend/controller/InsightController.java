package com.smartspend.backend.controller;

import com.smartspend.backend.dto.InsightDtos;
import com.smartspend.backend.service.InsightService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insights")
public class InsightController {
    private final InsightService insightService;

    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    @PostMapping
    public InsightDtos.InsightResponse analyze(@RequestBody InsightDtos.InsightRequest request) {
        return insightService.analyze(request);
    }
}
