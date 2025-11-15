package com.example.smartlearning.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizRequestDTO {
    @NotNull
    private Integer userSubjectId;

    private String topic; // Ví dụ: "Chương 1: Giới thiệu"
    private int numberOfQuestions = 5; // Mặc định là 5 câu
}