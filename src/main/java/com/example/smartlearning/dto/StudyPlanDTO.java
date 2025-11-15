package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudyPlanDTO {
    private Integer studyPlanId;
    private String planContent; // Nội dung Markdown/Text
    private LocalDateTime generatedAt;
    private String aiModelUsed;
}