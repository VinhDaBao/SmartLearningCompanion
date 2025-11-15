// Đặt tại: src/main/java/com/example/smartlearning/dto/GraphNodeDTO.java
package com.example.smartlearning.dto;

import lombok.Data;

@Data
public class GraphNodeDTO {
    private Integer id;
    private String label;
    private String group;

    // --- THÊM CÁC TRƯỜNG MỚI NÀY ---
    private Integer quizCount;
    private Integer flashcardCount;
    private String title; // Dùng làm tooltip
    // --- KẾT THÚC THÊM ---

    // Cần phải cập nhật Constructor
    public GraphNodeDTO(Integer id, String label, String group, Integer quizCount, Integer flashcardCount) {
        this.id = id;
        this.label = label;
        this.group = group;
        this.quizCount = quizCount;
        this.flashcardCount = flashcardCount;
        this.title = label +
                "\n- Quiz: " + quizCount +
                "\n- Flashcard: " + flashcardCount;
    }
}