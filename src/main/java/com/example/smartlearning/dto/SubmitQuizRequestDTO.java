// Đặt tại: src/main/java/com/example/smartlearning/dto/SubmitQuizRequestDTO.java
package com.example.smartlearning.dto;

import java.util.Map;

// Dùng Lombok (nếu bạn có) hoặc tự tạo getter/setter
public class SubmitQuizRequestDTO {

    private Integer userSubjectId;
    private Integer quizId;

    // Key: (Integer) questionId, Value: (String) selectedAnswer (ví dụ: "A")
    private Map<Integer, String> answers;

    // --- Getters and Setters ---

    public Integer getUserSubjectId() {
        return userSubjectId;
    }

    public void setUserSubjectId(Integer userSubjectId) {
        this.userSubjectId = userSubjectId;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public Map<Integer, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, String> answers) {
        this.answers = answers;
    }
}