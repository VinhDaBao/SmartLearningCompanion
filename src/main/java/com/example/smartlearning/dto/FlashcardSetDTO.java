package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FlashcardSetDTO {
    private Integer setId;
    private Integer userSubjectId;
    private String title;
    private LocalDateTime generatedAt;
    private String aiModelUsed;

    private List<FlashcardDTO> flashcards;
}