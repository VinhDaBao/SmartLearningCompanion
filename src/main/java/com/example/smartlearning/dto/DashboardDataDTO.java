package com.example.smartlearning.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardDataDTO {
    // Dùng cho Chart.js loại "Bar" hoặc "Line"
    // Ví dụ: labels = ["T2", "T3", "T4"]
    private List<String> timeLabels;

    // Ví dụ: data = [30, 45, 60] (phút)
    private List<Integer> timeData;

    // Dùng cho Chart.js loại "Doughnut" (Bánh vòng)
    // Ví dụ: {"QUIZ": 5, "PLAN": 2}
    private Map<String, Long> activityCounts;
}