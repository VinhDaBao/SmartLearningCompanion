package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudyPlanInfoDTO {
    private Integer studyPlanId;
    private String planContent;
    private LocalDateTime generatedAt;
    private String aiModelUsed;
}