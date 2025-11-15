package com.example.smartlearning.service;

import com.example.smartlearning.dto.DashboardDataDTO;
import com.example.smartlearning.model.LearningLog;
import com.example.smartlearning.model.User;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.LearningLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class LearningLogService {

    @Autowired
    private LearningLogRepository learningLogRepository;

    /**
     * Phương thức chung để ghi nhật ký hoạt động
     * (Giữ nguyên code của phương thức logActivity(...) )
     */
    public void logActivity(User user, UserSubject userSubject, String activityType, String details, Integer duration) {
        try {
            LearningLog log = new LearningLog();
            log.setUser(user);
            log.setUserSubject(userSubject);
            log.setActivityType(activityType);
            log.setDetails(details);
            log.setDurationMinutes(duration);

            learningLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Lỗi khi ghi LearningLog: " + e.getMessage());
        }
    }

    /**
     * Lấy và xử lý dữ liệu Dashboard cho một user
     */
    public DashboardDataDTO getDashboardData(Integer userId) {

        // --- (ĐÂY LÀ DÒNG ĐÃ SỬA) ---
        // Tên hàm đã được đổi thành findByUserUserId...
        List<LearningLog> logs = learningLogRepository.findByUserUserIdOrderByLogTimeDesc(userId)
                .stream().limit(30).collect(Collectors.toList());
        // --- (HẾT PHẦN SỬA) ---

        DashboardDataDTO dashboardData = new DashboardDataDTO();

        Map<String, Long> counts = logs.stream()
                .collect(Collectors.groupingBy(LearningLog::getActivityType, Collectors.counting()));
        dashboardData.setActivityCounts(counts);

        List<LearningLog> studySessionLogs = logs.stream()
                .filter(log -> log.getDurationMinutes() != null && log.getDurationMinutes() > 0)
                .limit(7)
                .collect(Collectors.toList());

        Collections.reverse(studySessionLogs);

        List<String> labels = studySessionLogs.stream()
                .map(log -> log.getLogTime().format(DateTimeFormatter.ofPattern("dd/MM")))
                .collect(Collectors.toList());
        dashboardData.setTimeLabels(labels);

        List<Integer> data = studySessionLogs.stream()
                .map(LearningLog::getDurationMinutes)
                .collect(Collectors.toList());
        dashboardData.setTimeData(data);

        return dashboardData;
    }
}