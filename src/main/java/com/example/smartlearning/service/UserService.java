package com.example.smartlearning.service;

import com.example.smartlearning.dto.RegisterRequestDTO; // MỚI
import com.example.smartlearning.model.User;
import com.example.smartlearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // MỚI
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // MỚI

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // MỚI: Tiêm (inject) PasswordEncoder
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Tìm một user bằng username.
     * (Giữ nguyên phương thức cũ)
     */
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    // --- PHƯƠNG THỨC MỚI CHO ĐĂNG KÝ ---

    /**
     * Logic nghiệp vụ: Đăng ký một user mới
     * @param requestDTO Dữ liệu từ form đăng ký
     * @return User entity vừa được tạo
     */
    @Transactional // Đảm bảo việc lưu là an toàn
    public User registerUser(RegisterRequestDTO requestDTO) {

        // 1. Kiểm tra xem username đã tồn tại chưa
        if (userRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        // 2. Tạo đối tượng User mới
        User newUser = new User();
        newUser.setUsername(requestDTO.getUsername());
        newUser.setEmail(requestDTO.getEmail());
        newUser.setFullName(requestDTO.getFullName());
        newUser.setLearningStyle(requestDTO.getLearningStyle());

        // 3. HASH MẬT KHẨU (Quan trọng)
        // Dùng Bean PasswordEncoder để mã hóa
        newUser.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));

        // 4. Gán vai trò (role)
        newUser.setRole("student"); // Mặc định là student

        // 5. Lưu vào database
        return userRepository.save(newUser);
    }
}