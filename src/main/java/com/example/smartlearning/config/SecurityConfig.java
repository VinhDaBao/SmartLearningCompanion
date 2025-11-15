package com.example.smartlearning.config;

import com.example.smartlearning.security.JwtAuthenticationFilter; // MỚI
import com.example.smartlearning.service.UserDetailsServiceImpl; // MỚI
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; // MỚI
import org.springframework.security.authentication.AuthenticationProvider; // MỚI
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // MỚI
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // MỚI
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // MỚI
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // MỚI
import org.springframework.security.config.http.SessionCreationPolicy; // MỚI
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; // MỚI
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // MỚI
import org.springframework.web.cors.CorsConfiguration; // MỚI
import org.springframework.web.cors.CorsConfigurationSource; // MỚI
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // MỚI

import java.util.List; // MỚI

@Configuration
@EnableWebSecurity // Kích hoạt Spring Security
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter; // "Người gác cổng"

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // Dịch vụ tải User

    /**
     * Bean PasswordEncoder (Giữ nguyên)
     * Dùng để hash mật khẩu
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * MỚI: AuthenticationProvider (Nhà cung cấp Xác thực)
     * Đây là "cỗ máy" Spring Security dùng để xác thực.
     * Chúng ta "bảo" nó:
     * 1. Hãy dùng "userDetailsService" (của chúng ta) để tìm User.
     * 2. Hãy dùng "passwordEncoder" (của chúng ta) để so sánh mật khẩu.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * MỚI: AuthenticationManager (Trình quản lý Xác thực)
     * Cần thiết cho (một số) quy trình xác thực.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * MỚI: Cấu hình CORS (Rất quan trọng cho Frontend)
     * Cho phép frontend (chạy ở localhost khác) gọi API này.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Cho phép frontend của bạn (ví dụ: chạy ở localhost:3000)
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000")); // THAY THẾ NẾU CẦN
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // Cho phép tất cả các header
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Áp dụng cho tất cả API
        return source;
    }


    /**
     * MỚI: SecurityFilterChain (Bảng điều khiển An ninh)
     * Đây là nơi "ra lệnh" bảo vệ API nào, mở API nào.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Mở API đăng nhập/đăng ký
                        .requestMatchers("/api/auth/**").permitAll()

                        // MỞ CÁC TRANG HTML
                        .requestMatchers("/", "/login", "/register").permitAll()

                        // --- (ĐÂY LÀ DÒNG SỬA) ---
                        // Mở trang "vỏ" (shell) của dashboard
                        .requestMatchers("/dashboard").permitAll()
                        // --- (HẾT PHẦN SỬA) ---

                        // MỞ CÁC FILE CSS/JS (NẾU CÓ)
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // ĐÓNG tất cả các API còn lại
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}