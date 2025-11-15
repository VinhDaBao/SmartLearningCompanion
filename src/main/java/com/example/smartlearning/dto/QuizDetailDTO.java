package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuizDetailDTO {
    private Integer quizId;
    private String title;
    private LocalDateTime generatedAt;

    private Integer userSubjectId;

    private List<QuizQuestionDetailDTO> questions;
}