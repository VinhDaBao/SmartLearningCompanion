// Đặt tại: src/main/java/com/example/smartlearning/repository/UserQuestionAnswerRepository.java
package com.example.smartlearning.repository;

import com.example.smartlearning.model.Topic;
import com.example.smartlearning.model.UserQuestionAnswer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserQuestionAnswerRepository extends JpaRepository<UserQuestionAnswer, Integer> {

    /**
     * Query "chẩn đoán" (Giữ nguyên)
     */
    @Query("SELECT qq.topic " +
            "FROM UserQuestionAnswer uqa " +
            "JOIN uqa.question qq " +
            "JOIN uqa.attempt ua " +
            "WHERE ua.userSubject.id = :userSubjectId " +
            "AND uqa.isCorrect = false " +
            "AND qq.topic IS NOT NULL " +
            "GROUP BY qq.topic " +
            "ORDER BY COUNT(uqa.answerId) DESC")
    List<Topic> findWeakestTopicsByUserSubjectId(
            @Param("userSubjectId") Integer userSubjectId,
            Pageable pageable
    );
    /**
     * Đếm tổng số câu trả lời (ĐÚNG hoặc SAI) của một UserSubject
     */
    long countByAttempt_UserSubject_IdAndIsCorrect(Integer userSubjectId, boolean isCorrect);}