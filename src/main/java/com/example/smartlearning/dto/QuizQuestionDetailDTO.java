package com.example.smartlearning.dto;

import lombok.Data;

@Data
public class QuizQuestionDetailDTO {
    private Integer questionId;
    private String questionText;

    // Object (Map) của các lựa chọn (A, B, C, D)
    private Object options;

    // QUAN TRỌNG: Gửi đáp án đúng về cho frontend
    private String correctAnswer;

    private String explanation;
}