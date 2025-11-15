package com.example.smartlearning.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class OpenAiRequestDTO {
    private String model;
    private List<ChatMessageDTO> messages;
	private int max_tokens;

    public OpenAiRequestDTO(String model, List<ChatMessageDTO> messages) {
        this.model = model;
        this.messages = messages;
        this.max_tokens =   1000;

    }
}