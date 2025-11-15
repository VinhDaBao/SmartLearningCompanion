//package com.example.smartlearning.dto;
//
//import lombok.Data;
//import java.util.List;
//import java.util.Map;
//
//@Data
//public class DashboardDataDTO {
//    // Dùng cho Chart.js loại "Bar" hoặc "Line"
//    // Ví dụ: labels = ["T2", "T3", "T4"]
//    private List<String> timeLabels;
//
//    // Ví dụ: data = [30, 45, 60] (phút)
//    private List<Integer> timeData;
//
//    // Dùng cho Chart.js loại "Doughnut" (Bánh vòng)
//    // Ví dụ: {"QUIZ": 5, "PLAN": 2}
//    private Map<String, Long> activityCounts;
//}
package com.example.smartlearning.dto;

import java.util.List;
import java.util.Map;

public class DashboardDataDTO {

    private List<String> timeLabels;
    private List<Integer> timeData;
    private Map<String, Long> activityCounts;
    private String studyNotification;

    public List<String> getTimeLabels() {
        return timeLabels;
    }

    public void setTimeLabels(List<String> timeLabels) {
        this.timeLabels = timeLabels;
    }

    public List<Integer> getTimeData() {
        return timeData;
    }

    public void setTimeData(List<Integer> timeData) {
        this.timeData = timeData;
    }

    public Map<String, Long> getActivityCounts() {
        return activityCounts;
    }

    public void setActivityCounts(Map<String, Long> activityCounts) {
        this.activityCounts = activityCounts;
    }

    public String getStudyNotification() {
        return studyNotification;
    }

    public void setStudyNotification(String studyNotification) {
        this.studyNotification = studyNotification;
    }
}