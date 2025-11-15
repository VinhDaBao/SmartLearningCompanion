// Đặt tại: src/main/java/com/example/smartlearning/model/UserQuizAttempt.java
package com.example.smartlearning.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "UserQuizAttempt")
public class UserQuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Integer attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subject_id", nullable = false)
    private UserSubject userSubject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "attempt_time")
    private LocalDateTime attemptTime;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserQuestionAnswer> answers;

    @PrePersist
    protected void onCreate() {
        attemptTime = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Integer getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Integer attemptId) {
        this.attemptId = attemptId;
    }

    public UserSubject getUserSubject() {
        return userSubject;
    }

    public void setUserSubject(UserSubject userSubject) {
        this.userSubject = userSubject;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public LocalDateTime getAttemptTime() {
        return attemptTime;
    }

    public void setAttemptTime(LocalDateTime attemptTime) {
        this.attemptTime = attemptTime;
    }

    public List<UserQuestionAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<UserQuestionAnswer> answers) {
        this.answers = answers;
    }
}