package com.example.smartlearning.dto;

import lombok.Data;

@Data
public class FlashcardDTO {
    private Integer flashcardId;
    private String frontText;
    private String backText;
}