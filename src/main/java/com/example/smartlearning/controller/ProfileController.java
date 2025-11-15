package com.example.smartlearning.controller;

import com.example.smartlearning.dto.UpdateProfileRequestDTO;
import com.example.smartlearning.dto.UserDTO;
import com.example.smartlearning.model.User;
import com.example.smartlearning.service.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // MỚI
import org.springframework.security.core.userdetails.UserDetails; // MỚI
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile") // API riêng cho profile
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * API LẤY thông tin profile của user ĐANG ĐĂNG NHẬP
     * URL: GET /api/profile
     */
    @GetMapping
    public ResponseEntity<UserDTO> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        // userDetails được Spring Security tự động tiêm vào
        // (chứa thông tin của user gắn với token)
        String username = userDetails.getUsername();

        User user = userService.findUserByUsername(username);
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return ResponseEntity.ok(userDTO);
    }

    /**
     * API CẬP NHẬT thông tin profile của user ĐANG ĐĂNG NHẬP
     * URL: PUT /api/profile
     */
    @PutMapping
    public ResponseEntity<UserDTO> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequestDTO dto) {

        String username = userDetails.getUsername();

        User updatedUser = userService.updateUserProfile(username, dto);
        UserDTO userDTO = modelMapper.map(updatedUser, UserDTO.class);

        return ResponseEntity.ok(userDTO);
    }
}