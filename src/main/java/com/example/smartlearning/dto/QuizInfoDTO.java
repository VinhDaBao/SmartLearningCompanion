// Đặt tại: src/main/java/com/example/smartlearning/dto/QuizInfoDTO.java
package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuizInfoDTO {
    private Integer quizId;
    private String title;
    private LocalDateTime generatedAt;
    private int questionCount;

    // --- BẮT ĐẦU THÊM MỚI ---
    private Double lastAttemptScore;
    private Integer incorrectCount;
    private LocalDateTime lastAttemptTime;
    // --- KẾT THÚC THÊM MỚI ---
}