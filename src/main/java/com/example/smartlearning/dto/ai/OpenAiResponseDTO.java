package com.example.smartlearning.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class OpenAiResponseDTO {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private ChatMessageDTO message;
    }
}