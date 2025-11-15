package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority; // MỚI
import org.springframework.security.core.authority.SimpleGrantedAuthority; // MỚI
import org.springframework.security.core.userdetails.UserDetails; // MỚI

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection; // MỚI
import java.util.List;

@Data
@Entity
@Table(name = "Users")
// MỚI: implement UserDetails
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "learning_style")
    private String learningStyle;

    @Column(name = "role", nullable = false)
    private String role; // Ví dụ: "student"

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // --- (Các mối quan hệ @OneToMany giữ nguyên) ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSubject> userSubjects = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LearningLog> learningLogs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // --- MỚI: CÁC PHƯƠNG THỨC CỦA UserDetails ---

    /**
     * Lấy danh sách Quyền (Roles).
     * Chúng ta lưu role "student", nên chúng ta chuyển nó thành một "Authority"
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getPassword() {
        // Trả về mật khẩu đã hash
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        // Trả về trường mà chúng ta dùng để đăng nhập
        return this.username;
    }

    // --- 4 phương thức này chúng ta để "true" (cho hackathon) ---
    // (Trong thực tế, bạn có thể kiểm tra xem tài khoản có bị khóa không)

    @Override
    public boolean isAccountNonExpired() {
        return true; // Tài khoản không hết hạn
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Tài khoản không bị khóa
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // "Bằng chứng" (mật khẩu) không hết hạn
    }

    @Override
    public boolean isEnabled() {
        return true; // Tài khoản được kích hoạt
    }
}