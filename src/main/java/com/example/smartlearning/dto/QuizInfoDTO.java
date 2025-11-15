package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuizInfoDTO {
    private Integer quizId;
    private String title;
    private LocalDateTime generatedAt;
    // ModelMapper sẽ tự động lấy size() của list 'questions'
    private int questionCount;
}