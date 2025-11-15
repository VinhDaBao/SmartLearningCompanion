package com.example.smartlearning.dto.ai;

import lombok.Data;

@Data // Dùng để Jackson parse JSON
public class AiFlashcardDTO {
    private String front; // Mặt trước
    private String back;  // Mặt sau
}