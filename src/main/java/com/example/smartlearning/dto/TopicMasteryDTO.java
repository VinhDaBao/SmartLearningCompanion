// Đặt tại: src/main/java/com/example/smartlearning/dto/TopicMasteryDTO.java
package com.example.smartlearning.dto;

import lombok.Data;

@Data
public class TopicMasteryDTO {
    private Integer topicId;
    private Long correctCount;
    private Long totalCount;
    private double masteryScore;

    // Constructor này RẤT QUAN TRỌNG, nó phải khớp với query SQL
    public TopicMasteryDTO(Integer topicId, Long correctCount, Long totalCount) {
        this.topicId = topicId;
        this.correctCount = correctCount;
        this.totalCount = (totalCount == null || totalCount == 0) ? 1 : totalCount; // Tránh chia 0
        this.masteryScore = (double) (correctCount == null ? 0 : correctCount) / this.totalCount;
    }
}