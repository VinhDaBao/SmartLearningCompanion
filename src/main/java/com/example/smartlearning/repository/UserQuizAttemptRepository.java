// Đặt tại: src/main/java/com/example/smartlearning/repository/UserQuizAttemptRepository.java
package com.example.smartlearning.repository;

import com.example.smartlearning.model.Quiz;
import com.example.smartlearning.model.UserQuizAttempt;
import com.example.smartlearning.model.UserSubject;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Integer> {

    long countByUserSubject_Id(Integer userSubjectId);
    Optional<UserQuizAttempt> findTopByUserSubject_IdAndQuiz_QuizIdOrderByAttemptTimeDesc(Integer userSubjectId, Integer quizId);
    boolean existsByUserSubjectAndQuiz(UserSubject userSubject, Quiz quiz);

}