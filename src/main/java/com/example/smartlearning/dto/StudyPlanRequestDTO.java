package com.example.smartlearning.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudyPlanRequestDTO {
    // Frontend chỉ cần gửi ID của môn học họ đang xem
    @NotNull
    private Integer userSubjectId;

    // (Optional) Frontend có thể gửi thêm yêu cầu tùy chỉnh
    private String customPrompt; // Ví dụ: "Tập trung vào phần thực hành"
}