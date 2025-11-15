// Đặt tại: src/main/java/com/example/smartlearning/dto/SubmitQuizResponseDTO.java
package com.example.smartlearning.dto;

import java.util.List;

public class SubmitQuizResponseDTO {

    private Integer attemptId;
    private double score; // Điểm (ví dụ: 8.5)
    private int totalQuestions;
    private int correctCount;
    private List<QuestionFeedbackDTO> feedback; // Phản hồi chi tiết từng câu

    // --- Getters and Setters ---

    public Integer getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Integer attemptId) {
        this.attemptId = attemptId;
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

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public List<QuestionFeedbackDTO> getFeedback() {
        return feedback;
    }

    public void setFeedback(List<QuestionFeedbackDTO> feedback) {
        this.feedback = feedback;
    }
}