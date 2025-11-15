package com.example.smartlearning.controller;

import com.example.smartlearning.dto.ai.VideoSuggestionDTO;
import com.example.smartlearning.service.AiGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiGenerationService aiGenerationService;

    @GetMapping("/suggest-video")
    public ResponseEntity<VideoSuggestionDTO> suggestVideo(@RequestParam String topic) {
        VideoSuggestionDTO suggestion = aiGenerationService.googleSearchAndSuggestVideo(topic);
        return ResponseEntity.ok(suggestion);
    }
}