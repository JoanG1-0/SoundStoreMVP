package com.soundstore.backend.controller;

import com.soundstore.backend.dto.admin.DashboardResponseDto;
import com.soundstore.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponseDto getMetricas() {
        return dashboardService.getMetricas();
    }
}
