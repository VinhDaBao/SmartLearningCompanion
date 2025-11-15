package com.example.smartlearning.dto;

import lombok.Data;

@Data // Chỉ cần Lombok là đủ
public class UserDTO {
    // Đây là những trường an toàn để trả về cho Frontend
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String learningStyle;
    private String role;
}