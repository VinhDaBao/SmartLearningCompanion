// Đặt tại: src/main/java/com/example/smartlearning/dto/FlashcardReviewDTO.java
package com.example.smartlearning.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class FlashcardReviewDTO {

    @NotNull
    private Integer flashcardId;

    @NotNull
    private Boolean wasRemembered; // true = Nhớ, false = Không nhớ
}