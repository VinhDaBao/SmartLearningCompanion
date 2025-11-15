package com.example.smartlearning.dto;

import lombok.Data;

@Data
public class QuizQuestionDTO {
    private Integer questionId;
    private String questionText;

    // Chúng ta sẽ chuyển chuỗi JSON "options" thành một đối tượng
    // mà Javascript có thể đọc trực tiếp (Map hoặc JSON)
    private Object options;

    // Chúng ta KHÔNG trả về "correctAnswer" ngay lập tức
    // Chỉ trả về "explanation" sau khi user đã trả lời
    private String explanation;
}