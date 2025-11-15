package com.example.smartlearning.controller;

import com.example.smartlearning.dto.DashboardDataDTO;
import com.example.smartlearning.service.LearningLogService;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private LearningLogService learningLogService;


    @GetMapping("/{userId}")
    public ResponseEntity<DashboardDataDTO> getDashboardData(
            @PathVariable Integer userId,
            @RequestParam(name = "filterType", defaultValue = "week") String filterType,
            @RequestParam(name = "filterDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate filterDate) {

        if (filterDate == null) {
            filterDate = LocalDate.now();
        }

        DashboardDataDTO data = learningLogService.getDashboardData(userId, filterType, filterDate);

        return ResponseEntity.ok(data);
    }
}