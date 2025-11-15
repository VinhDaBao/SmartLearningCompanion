package com.example.smartlearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {

    private String token; // JWT Token
    private String username;
    // Bạn có thể trả về thêm UserDTO nếu muốn
}