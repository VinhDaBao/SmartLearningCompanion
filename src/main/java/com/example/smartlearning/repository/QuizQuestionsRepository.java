// Đặt tại: src/main/java/com/example/smartlearning/repository/QuizQuestionsRepository.java
package com.example.smartlearning.repository;

import com.example.smartlearning.model.QuizQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizQuestionsRepository extends JpaRepository<QuizQuestions, Integer> {

    // Tự động tìm tất cả câu hỏi thuộc về một quizId
    List<QuizQuestions> findByQuiz_QuizId(Integer quizId);
    long countByTopic_TopicId(Integer topicId);
    int countByQuiz_QuizId(Integer quizId);
}