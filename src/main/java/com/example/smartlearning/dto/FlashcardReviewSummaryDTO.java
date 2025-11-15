// Đặt tại: src/main/java/com/example/smartlearning/dto/FlashcardReviewSummaryDTO.java
package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime; // <-- THÊM IMPORT

@Data
public class FlashcardReviewSummaryDTO {

    // Số thẻ đã "tới hạn" (cho thông báo)
    private int dueCount;

    // Số thẻ ở Mốc 1 (Mới học)
    private long milestone1Count;

    // Số thẻ ở Mốc 2 (Đã nhớ)
    private long milestone2Count;

    // Số thẻ ở Mốc 3 (Ghi nhớ)
    private long milestone3Count;

    // Số thẻ ở Mốc 4 (Đã thuộc)
    private long milestone4Count;

    // Số thẻ ở Mốc 5+ (Master)
    private long milestone5PlusCount;

    // =================================================================
    // === BẮT ĐẦU THÊM MỚI (CHO ĐỒNG HỒ ĐẾM NGƯỢC) ===
    // =================================================================
    private LocalDateTime nextReviewTime; // Thời gian ôn tập gần nhất
}