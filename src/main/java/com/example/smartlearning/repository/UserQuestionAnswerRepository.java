// Đặt tại: src/main/java/com/example/smartlearning/repository/UserQuestionAnswerRepository.java
package com.example.smartlearning.repository;

import com.example.smartlearning.model.Topic;
import com.example.smartlearning.model.UserQuestionAnswer;
import org.springframework.data.domain.Pageable;
import com.example.smartlearning.dto.TopicMasteryDTO;
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
    long countByAttempt_UserSubject_IdAndIsCorrect(Integer userSubjectId, boolean isCorrect);
    @Query("SELECT new com.example.smartlearning.dto.TopicMasteryDTO(" +
            "  qq.topic.topicId, " +
            "  SUM(CASE WHEN uqa.isCorrect = true THEN 1 ELSE 0 END), " +
            "  COUNT(uqa.answerId) " +
            ") " +
            "FROM UserQuestionAnswer uqa " +
            "JOIN uqa.question qq " +
            "JOIN uqa.attempt ua " +
            "WHERE ua.userSubject.id = :userSubjectId " +
            "AND qq.topic.topicId IS NOT NULL " +
            "GROUP BY qq.topic.topicId")
    List<TopicMasteryDTO> getMasteryScoresForUserSubject(@Param("userSubjectId") Integer userSubjectId);
    
    @Query("SELECT COUNT(uqa) FROM UserQuestionAnswer uqa WHERE uqa.attempt.attemptId = :attemptId AND uqa.isCorrect = false")
    int countIncorrectByAttemptId(Integer attemptId);
}