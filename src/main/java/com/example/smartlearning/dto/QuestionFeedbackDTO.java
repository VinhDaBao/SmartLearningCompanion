// Đặt tại: src/main/java/com/example/smartlearning/dto/QuestionFeedbackDTO.java
package com.example.smartlearning.dto;

public class QuestionFeedbackDTO {

    private Integer questionId;
    private String selectedAnswer;
    private String correctAnswer;
    private boolean isCorrect;
    private String explanation;

    // --- Constructors ---
    public QuestionFeedbackDTO(Integer questionId, String selectedAnswer, String correctAnswer, boolean isCorrect, String explanation) {
        this.questionId = questionId;
        this.selectedAnswer = selectedAnswer;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.explanation = explanation;
    }

    // --- Getters and Setters ---

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}