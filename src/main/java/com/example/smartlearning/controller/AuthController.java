package com.example.smartlearning.controller;

import com.example.smartlearning.dto.AuthResponseDTO;
import com.example.smartlearning.dto.LoginRequestDTO;
import com.example.smartlearning.dto.RegisterRequestDTO;
import com.example.smartlearning.dto.UserDTO;
import com.example.smartlearning.model.User;
import com.example.smartlearning.service.AuthService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Tiền tố chung cho API xác thực
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * API để Đăng ký tài khoản mới
     * URL: POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        // 1. Gọi service để thực hiện logic đăng ký
        User newUser = authService.register(registerRequest);

        // 2. Map User (Entity) -> UserDTO (an toàn)
        UserDTO responseUser = modelMapper.map(newUser, UserDTO.class);

        // 3. Trả về DTO của user vừa tạo
        return new ResponseEntity<>(responseUser, HttpStatus.CREATED);
    }

    /**
     * API để Đăng nhập
     * URL: POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginRequest) {

        // 1. Gọi service để thực hiện logic đăng nhập
        // Service sẽ kiểm tra user/pass và tạo token
        AuthResponseDTO authResponse = authService.login(loginRequest);
        
        // 2. Trả về DTO chứa token
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Xử lý các lỗi RuntimeException (ví dụ: "Username already taken")
     * mà các service đã ném (throw)
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        // Trả về một thông báo lỗi 400 Bad Request
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}