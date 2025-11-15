package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuizDTO {
    private Integer quizId;
    private String title;
    private LocalDateTime generatedAt;

    // Lồng DTO
    private List<QuizQuestionDTO> questions;
}