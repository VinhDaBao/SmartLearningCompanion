package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserSubjectDTO {
    private Integer id; // Đây chính là userSubjectId
    private Integer progressPercentage;
    private Double currentScore;
    private LocalDateTime enrolledAt;

    // Chúng ta lồng DTO để biết đây là môn nào
    private SubjectDTO subject;

    // (Không cần lồng UserDTO để tránh lộ thông tin)
}