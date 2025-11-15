package com.example.smartlearning.repository;

import com.example.smartlearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Báo cho Spring biết đây là một Bean Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring Data JPA sẽ tự động tạo một câu query SQL
    // CHỌN * TỪ Users NƠI username = ?
    // Chỉ bằng cách bạn đặt tên phương thức như thế này:
    Optional<User> findByUsername(String username);

    // Bạn cũng có thể dùng:
    // Optional<User> findByEmail(String email);
    // Boolean existsByUsername(String username);
}