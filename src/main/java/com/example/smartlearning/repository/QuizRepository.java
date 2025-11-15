package com.example.smartlearning.repository;

import com.example.smartlearning.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    // Lấy tất cả các quiz cho một môn học của user
    List<Quiz> findByUserSubjectId(Integer userSubjectId);
}