package com.example.smartlearning.service;

import com.example.smartlearning.dto.AuthResponseDTO;
import com.example.smartlearning.dto.LoginRequestDTO;
import com.example.smartlearning.dto.RegisterRequestDTO;
import com.example.smartlearning.model.User;
import com.example.smartlearning.repository.UserRepository;
import com.example.smartlearning.security.JwtService; // Import JwtService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService; // Dùng lại logic đăng ký

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService; // Tiêm (inject) JwtService

    /**
     * Xử lý logic Đăng ký User
     * @param requestDTO Dữ liệu form đăng ký
     * @return UserDTO (hoặc AuthResponseDTO nếu bạn muốn tự động login)
     */
    public User register(RegisterRequestDTO requestDTO) {
        // Gọi logic đăng ký đã viết ở UserService
        return userService.registerUser(requestDTO);
    }

    /**
     * Xử lý logic Đăng nhập
     * @param requestDTO Dữ liệu form đăng nhập (username, password)
     * @return AuthResponseDTO (chứa token)
     */
    public AuthResponseDTO login(LoginRequestDTO requestDTO) {

        // 1. Tìm user trong database
        User user = userRepository.findByUsername(requestDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("Lỗi: Thông tin đăng nhập không hợp lệ.")); // Không nên nói rõ là sai username

        // 2. So sánh mật khẩu
        // Dùng passwordEncoder.matches(mật_khẩu_thô, mật_khẩu_đã_hash)
        if (!passwordEncoder.matches(requestDTO.getPassword(), user.getPasswordHash())) {
            // Nếu mật khẩu không khớp
            throw new RuntimeException("Lỗi: Thông tin đăng nhập không hợp lệ."); // Không nên nói rõ là sai password
        }

        // 3. Mật khẩu khớp -> Tạo Token
        String jwtToken = jwtService.generateToken(user.getUsername());

        // 4. Trả về DTO chứa token
        return new AuthResponseDTO(jwtToken, user.getUsername());
    }
}