package com.example.smartlearning.dto;

import lombok.Data;

@Data
public class QuizSubmissionDTO {
    private Integer quizId;
    private Integer userId;
    private Integer durationInMinutes;
    private Integer score;
}