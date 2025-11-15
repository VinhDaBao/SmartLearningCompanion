package com.example.smartlearning.repository;

import com.example.smartlearning.model.LearningLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LearningLogRepository extends JpaRepository<LearningLog, Integer> {

    List<LearningLog> findByUserUserIdOrderByLogTimeDesc(Integer userId);

    List<LearningLog> findByUserUserIdAndLogTimeBetween(Integer userId, LocalDateTime startTime, LocalDateTime endTime);
}