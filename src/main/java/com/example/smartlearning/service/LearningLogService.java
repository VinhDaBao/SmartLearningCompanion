//package com.example.smartlearning.service;
//
//import com.example.smartlearning.dto.DashboardDataDTO;
//import com.example.smartlearning.model.LearningLog;
//import com.example.smartlearning.model.User;
//import com.example.smartlearning.model.UserSubject;
//import com.example.smartlearning.repository.LearningLogRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.format.DateTimeFormatter;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//
//@Service
//public class LearningLogService {
//
//    @Autowired
//    private LearningLogRepository learningLogRepository;
//
//    /**
//     * Phương thức chung để ghi nhật ký hoạt động
//     * (Giữ nguyên code của phương thức logActivity(...) )
//     */
//    public void logActivity(User user, UserSubject userSubject, String activityType, String details, Integer duration) {
//        try {
//            LearningLog log = new LearningLog();
//            log.setUser(user);
//            log.setUserSubject(userSubject);
//            log.setActivityType(activityType);
//            log.setDetails(details);
//            log.setDurationMinutes(duration);
//
//            learningLogRepository.save(log);
//        } catch (Exception e) {
//            System.err.println("Lỗi khi ghi LearningLog: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Lấy và xử lý dữ liệu Dashboard cho một user
//     */
//    public DashboardDataDTO getDashboardData(Integer userId) {
//
//        // --- (ĐÂY LÀ DÒNG ĐÃ SỬA) ---
//        // Tên hàm đã được đổi thành findByUserUserId...
//        List<LearningLog> logs = learningLogRepository.findByUserUserIdOrderByLogTimeDesc(userId)
//                .stream().limit(30).collect(Collectors.toList());
//        // --- (HẾT PHẦN SỬA) ---
//
//        DashboardDataDTO dashboardData = new DashboardDataDTO();
//
//        Map<String, Long> counts = logs.stream()
//                .collect(Collectors.groupingBy(LearningLog::getActivityType, Collectors.counting()));
//        dashboardData.setActivityCounts(counts);
//
//        List<LearningLog> studySessionLogs = logs.stream()
//                .filter(log -> log.getDurationMinutes() != null && log.getDurationMinutes() > 0)
//                .limit(7)
//                .collect(Collectors.toList());
//
//        Collections.reverse(studySessionLogs);
//
//        List<String> labels = studySessionLogs.stream()
//                .map(log -> log.getLogTime().format(DateTimeFormatter.ofPattern("dd/MM")))
//                .collect(Collectors.toList());
//        dashboardData.setTimeLabels(labels);
//
//        List<Integer> data = studySessionLogs.stream()
//                .map(LearningLog::getDurationMinutes)
//                .collect(Collectors.toList());
//        dashboardData.setTimeData(data);
//
//        return dashboardData;
//    }
//}
package com.example.smartlearning.service;

import com.example.smartlearning.dto.DashboardDataDTO;
import com.example.smartlearning.model.LearningLog;
import com.example.smartlearning.model.User;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.LearningLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LearningLogService {

    @Autowired
    private LearningLogRepository learningLogRepository;

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

    public DashboardDataDTO getDashboardData(Integer userId, String filterType, LocalDate filterDate) {

        DashboardDataDTO dashboardData = new DashboardDataDTO();

        Map<String, Integer> studyTimeData = getStudyTimeData(userId, filterType, filterDate);
        dashboardData.setTimeLabels(new ArrayList<>(studyTimeData.keySet()));
        dashboardData.setTimeData(new ArrayList<>(studyTimeData.values()));

        List<LearningLog> allLogs = learningLogRepository.findByUserUserIdOrderByLogTimeDesc(userId);

        Map<String, Long> counts = allLogs.stream()
                .collect(Collectors.groupingBy(LearningLog::getActivityType, Collectors.counting()));
        dashboardData.setActivityCounts(counts);

        String notification = getDailyStudyComparison(userId);
        dashboardData.setStudyNotification(notification);

        return dashboardData;
    }

    private Map<String, Integer> getStudyTimeData(Integer userId, String filterType, LocalDate filterDate) {
        Map<String, Integer> data = new LinkedHashMap<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

        switch (filterType) {
            case "week":
                LocalDate startDate = filterDate.minusDays(6);
                Map<LocalDate, Integer> weeklyLogs = learningLogRepository.findByUserUserIdAndLogTimeBetween(userId, startDate.atStartOfDay(), filterDate.atTime(LocalTime.MAX))
                        .stream()
                        .filter(log -> log.getDurationMinutes() != null && log.getDurationMinutes() > 0)
                        .collect(Collectors.groupingBy(
                                log -> log.getLogTime().toLocalDate(),
                                Collectors.summingInt(LearningLog::getDurationMinutes)
                        ));

                for (int i = 6; i >= 0; i--) {
                    LocalDate day = filterDate.minusDays(i);
                    data.put(day.format(dayFormatter), weeklyLogs.getOrDefault(day, 0));
                }
                break;

            case "month":
                YearMonth yearMonth = YearMonth.from(filterDate);
                LocalDate firstDay = filterDate.withDayOfMonth(1);
                LocalDate lastDay = filterDate.withDayOfMonth(yearMonth.lengthOfMonth());

                Map<LocalDate, Integer> monthlyLogs = learningLogRepository.findByUserUserIdAndLogTimeBetween(userId, firstDay.atStartOfDay(), lastDay.atTime(LocalTime.MAX))
                        .stream()
                        .filter(log -> log.getDurationMinutes() != null && log.getDurationMinutes() > 0)
                        .collect(Collectors.groupingBy(
                                log -> log.getLogTime().toLocalDate(),
                                Collectors.summingInt(LearningLog::getDurationMinutes)
                        ));

                for (int i = 1; i <= lastDay.getDayOfMonth(); i++) {
                    LocalDate day = firstDay.plusDays(i - 1);
                    data.put(day.format(dayFormatter), monthlyLogs.getOrDefault(day, 0));
                }
                break;

            case "year":
                int year = filterDate.getYear();
                LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
                LocalDate lastDayOfYear = LocalDate.of(year, 12, 31);

                Map<Integer, Integer> yearlyLogs = learningLogRepository.findByUserUserIdAndLogTimeBetween(userId, firstDayOfYear.atStartOfDay(), lastDayOfYear.atTime(LocalTime.MAX))
                        .stream()
                        .filter(log -> log.getDurationMinutes() != null && log.getDurationMinutes() > 0)
                        .collect(Collectors.groupingBy(
                                log -> log.getLogTime().getMonthValue(),
                                Collectors.summingInt(LearningLog::getDurationMinutes)
                        ));

                for (int i = 1; i <= 12; i++) {
                    String monthLabel = LocalDate.of(year, i, 1).format(monthFormatter);
                    data.put(monthLabel, yearlyLogs.getOrDefault(i, 0));
                }
                break;
        }

        return data;
    }

    private String getDailyStudyComparison(Integer userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        int todayStudyTime = learningLogRepository.findByUserUserIdAndLogTimeBetween(userId, startOfToday, endOfToday)
                .stream()
                .filter(log -> log.getDurationMinutes() != null)
                .mapToInt(LearningLog::getDurationMinutes)
                .sum();

        LocalDateTime startOfPeriod = today.minusDays(7).atStartOfDay();
        LocalDateTime endOfPeriod = startOfToday.minusNanos(1);

        List<LearningLog> last7DaysLogs = learningLogRepository.findByUserUserIdAndLogTimeBetween(userId, startOfPeriod, endOfPeriod);

        double totalLast7Days = last7DaysLogs.stream()
                .filter(log -> log.getDurationMinutes() != null)
                .mapToDouble(LearningLog::getDurationMinutes)
                .sum();

        Map<LocalDate, Long> daysWithStudy = last7DaysLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getLogTime().toLocalDate(), Collectors.counting()));
        long distinctStudyDays = daysWithStudy.isEmpty() ? 1 : daysWithStudy.size();


        double averageStudyTime = (totalLast7Days / distinctStudyDays);

        if (todayStudyTime > 0) {
            if (todayStudyTime > averageStudyTime * 1.3) {
                return String.format("Tuyệt vời! Hôm nay bạn đã học được %d phút, cao hơn nhiều so với mức trung bình (%.0f phút/ngày) của tuần qua.", todayStudyTime, averageStudyTime);
            } else if (todayStudyTime < averageStudyTime * 0.7) {
                return String.format("Bạn đã học được %d phút hôm nay. Cố gắng thêm chút nữa để đạt mục tiêu trung bình (%.0f phút/ngày) nhé!", todayStudyTime, averageStudyTime);
            } else {
                return String.format("Duy trì tốt! Bạn đã học %d phút hôm nay, tương đương mức trung bình (%.0f phút/ngày).", todayStudyTime, averageStudyTime);
            }
        } else {
            if (averageStudyTime > 0) {
                return String.format("Hôm nay bạn chưa học. Mức trung bình của bạn tuần qua là %.0f phút/ngày. Hãy bắt đầu nào!", averageStudyTime);
            } else {
                return "Hãy bắt đầu buổi học đầu tiên của bạn ngay hôm nay!";
            }
        }
    }
}