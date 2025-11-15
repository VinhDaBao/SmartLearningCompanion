package com.example.smartlearning.controller;

import com.example.smartlearning.dto.ManualLogRequestDTO;
import com.example.smartlearning.model.User;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.service.LearningLogService;
import com.example.smartlearning.service.UserSubjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/log")
public class LearningLogController {

    @Autowired
    private LearningLogService learningLogService;

    @Autowired
    private UserSubjectService userSubjectService; // Cần để lấy UserSubject

    /**
     * API để user tự ghi lại một phiên học
     * URL: POST /api/log/session
     */
    @PostMapping("/session")
    public ResponseEntity<Void> logStudySession(
            @AuthenticationPrincipal User user, // Lấy user đã đăng nhập
            @Valid @RequestBody ManualLogRequestDTO logRequest) {

        // 1. Lấy (và xác thực) môn học
        // (Chúng ta không dùng userSubjectId từ DTO để bảo mật)
        UserSubject userSubject = userSubjectService.getEnrolledSubjectDetails(
                logRequest.getUserSubjectId()
        );

        // 2. Kiểm tra xem môn học này có thuộc user đang đăng nhập không
        if (!userSubject.getUser().getUserId().equals(user.getUserId())) {
            // Nếu user A cố log cho user B -> Từ chối
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }

        // 3. Gọi service để ghi log
        learningLogService.logActivity(
                user,
                userSubject,
                "STUDY_SESSION", // Loại hoạt động
                logRequest.getDetails(),
                logRequest.getDurationMinutes()
        );

        return ResponseEntity.ok().build(); // Trả về 200 OK
    }
}