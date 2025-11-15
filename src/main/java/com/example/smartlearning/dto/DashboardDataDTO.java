package com.example.smartlearning.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardDataDTO {

    private List<String> timeLabels;

    private List<Integer> timeData;

    private Map<String, Long> activityCounts;

    private String studyNotification;
}