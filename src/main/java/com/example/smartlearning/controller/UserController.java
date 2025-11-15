package com.example.smartlearning.controller;

import com.example.smartlearning.dto.UserDTO;
import com.example.smartlearning.model.User;
import com.example.smartlearning.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Đánh dấu đây là một API Controller (trả về JSON)
@RequestMapping("/api/users") // Tất cả API trong class này sẽ bắt đầu bằng /api/users
public class UserController {

    @Autowired
    private UserService userService;

    // Tiêm ModelMapper đã tạo ở AppConfig
    @Autowired
    private ModelMapper modelMapper;

    /**
     * API lấy thông tin user bằng username
     * URL: GET /api/users/an_nguyen
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        // 1. Gọi Service để lấy logic và Entity
        User user = userService.findUserByUsername(username);

        // 2. Chuyển Entity (model) -> DTO (an toàn)
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        // 3. Trả DTO về cho frontend với status 200 OK
        return ResponseEntity.ok(userDTO);
    }

    // (Bạn sẽ thêm các API khác ở đây, ví dụ:
    // @PostMapping("/register")
    // public ResponseEntity<UserDTO> registerUser(@RequestBody UserRegistrationDTO dto) { ... }
    // )
}