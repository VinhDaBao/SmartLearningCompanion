package com.example.smartlearning.controller;

import com.example.smartlearning.dto.StudyPlanDTO;
import com.example.smartlearning.dto.StudyPlanRequestDTO;
import com.example.smartlearning.model.StudyPlan;
import com.example.smartlearning.service.StudyPlanService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study-plans")
public class StudyPlanController {

    @Autowired
    private StudyPlanService studyPlanService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * API để Frontend kích hoạt việc sinh Lộ trình học mới
     * URL: POST /api/study-plans/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<StudyPlanDTO> generateStudyPlan(
            @Valid @RequestBody StudyPlanRequestDTO requestDTO) {

        // 1. Gọi Service điều phối (đã chứa logic gọi AI và lưu DB)
        StudyPlan newStudyPlan = studyPlanService.createStudyPlan(requestDTO);

        // 2. Map Entity kết quả -> DTO
        StudyPlanDTO responseDTO = modelMapper.map(newStudyPlan, StudyPlanDTO.class);

        // 3. Trả DTO về cho Frontend
        return ResponseEntity.ok(responseDTO);
    }
}