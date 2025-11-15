// Đặt tại: src/main/java/com/example/smartlearning/model/UserQuestionAnswer.java
package com.example.smartlearning.model;

import jakarta.persistence.*;

@Entity
@Table(name = "UserQuestionAnswer")
public class UserQuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Integer answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private UserQuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestions question;

    @Column(name = "selected_answer", length = 10, nullable = false)
    private String selectedAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    // --- Getters and Setters ---

    public Integer getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }

    public UserQuizAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(UserQuizAttempt attempt) {
        this.attempt = attempt;
    }

    public QuizQuestions getQuestion() {
        return question;
    }

    public void setQuestion(QuizQuestions question) {
        this.question = question;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}