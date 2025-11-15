// Đặt tại: src/main/java/com/example/smartlearning/repository/UserQuizAttemptRepository.java
package com.example.smartlearning.repository;

import com.example.smartlearning.model.UserQuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Integer> {

    /**
     * Đếm xem UserSubject này đã có bao nhiêu lần làm bài (attempts)
     */
    long countByUserSubject_Id(Integer userSubjectId);
}