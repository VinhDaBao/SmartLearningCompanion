// Đặt tại: src/main/java/com/example/smartlearning/dto/AdaptiveRecommendationDTO.java
package com.example.smartlearning.dto;

import com.example.smartlearning.model.SubjectContent;
import lombok.Data;

@Data
public class AdaptiveRecommendationDTO {

    // Tin nhắn do AI tạo (VD: "Tôi thấy bạn yếu chủ đề JPA...")
    private String recommendationText;

    // Tài liệu được đề xuất
    private SubjectContent recommendedContent;

    // True nếu đây là đề xuất, False nếu user đã giỏi
    private boolean recommendationProvided;

    public AdaptiveRecommendationDTO(String text, SubjectContent content, boolean provided) {
        this.recommendationText = text;
        this.recommendedContent = content;
        this.recommendationProvided = provided;
    }
}