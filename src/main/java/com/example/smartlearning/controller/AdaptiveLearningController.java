package com.example.smartlearning.controller;

import com.example.smartlearning.dto.AdaptiveRecommendationDTO;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.UserSubjectRepository;
import com.example.smartlearning.service.AdaptiveLearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adaptive")
public class AdaptiveLearningController {

    @Autowired
    private AdaptiveLearningService adaptiveService;

    @Autowired
    private UserSubjectRepository userSubjectRepository;

    /**
     * API Lấy đề xuất học tập thích ứng
     * URL: GET /api/adaptive/recommendation/5
     */
    @GetMapping("/recommendation/{userSubjectId}")
    public ResponseEntity<AdaptiveRecommendationDTO> getRecommendation(
            @PathVariable Integer userSubjectId
    ) {

        UserSubject userSubject = userSubjectRepository.findById(userSubjectId)
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));

        AdaptiveRecommendationDTO recommendation = adaptiveService.getAdaptiveRecommendation(userSubject);

        return ResponseEntity.ok(recommendation);
    }
}