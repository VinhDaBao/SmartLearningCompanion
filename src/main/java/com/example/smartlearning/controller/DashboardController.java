package com.example.smartlearning.controller;

import com.example.smartlearning.dto.DashboardDataDTO;
import com.example.smartlearning.service.LearningLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private LearningLogService learningLogService;

    /**
     * API Lấy dữ liệu Dashboard cho Chart.js
     * URL: GET /api/dashboard/{userId}
     * * QUAN TRỌNG: Trong thực tế khi đã có Security, bạn sẽ lấy
     * userId từ "Authentication Principal" (người dùng đang đăng nhập)
     * chứ không phải lấy từ PathVariable.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<DashboardDataDTO> getDashboardData(@PathVariable Integer userId) {

        // Gọi service để lấy dữ liệu đã xử lý
        DashboardDataDTO data = learningLogService.getDashboardData(userId);

        // Trả về JSON cho frontend
        return ResponseEntity.ok(data);
    }
}