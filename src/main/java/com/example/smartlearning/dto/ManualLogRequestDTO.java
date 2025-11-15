package com.example.smartlearning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManualLogRequestDTO {

    @NotNull
    private Integer userId; // Chúng ta sẽ lấy ID này từ user đang đăng nhập

    @NotNull(message = "Bạn phải chọn một môn học để log")
    private Integer userSubjectId; // Môn học đã log

    @NotNull
    @Min(value = 1, message = "Thời gian học phải lớn hơn 0")
    private Integer durationMinutes; // Số phút đã học

    private String details; // Ghi chú (ví dụ: "Đã hoàn thành Chương 1")
}