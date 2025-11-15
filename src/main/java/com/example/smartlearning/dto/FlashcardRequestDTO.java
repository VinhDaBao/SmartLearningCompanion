package com.example.smartlearning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlashcardRequestDTO {
    @NotNull
    private Integer userSubjectId;

    @NotBlank
    private String title; // Ví dụ: "Thuật ngữ Chương 1"

    private String topic; // Gợi ý cho AI
    private int numberOfCards = 10;
}