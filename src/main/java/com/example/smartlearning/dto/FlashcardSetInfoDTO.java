package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FlashcardSetInfoDTO {
    private Integer setId;
    private String title;
    private LocalDateTime generatedAt;
    private int flashcardCount;
}