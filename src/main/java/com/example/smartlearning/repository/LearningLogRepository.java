package com.example.smartlearning.repository;

import com.example.smartlearning.model.LearningLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningLogRepository extends JpaRepository<LearningLog, Integer> {
    // Rất quan trọng cho Dashboard
    List<LearningLog> findByUserUserIdOrderByLogTimeDesc(Integer userId);
}