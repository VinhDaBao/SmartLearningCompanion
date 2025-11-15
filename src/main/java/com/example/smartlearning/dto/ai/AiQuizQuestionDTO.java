package com.example.smartlearning.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data // Dùng cho Jackson parsing
public class AiQuizQuestionDTO {
    // Jackson sẽ map key "questionText" trong JSON
    // vào thuộc tính "questionText" này
    private String questionText;

    private Map<String, String> options;

    // @JsonProperty rất hữu ích nếu tên JSON và tên Java khác nhau
    @JsonProperty("correctAnswer")
    private String correctAnswer;

    private String explanation;
}