package com.example.smartlearning.controller;

import com.example.smartlearning.dto.StudyPlanDTO;
import com.example.smartlearning.dto.StudyPlanRequestDTO;
import com.example.smartlearning.model.StudyPlan;
import com.example.smartlearning.service.FileContentService;
import com.example.smartlearning.service.StudyPlanService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/study-plans")
public class StudyPlanController {

    @Autowired
    private StudyPlanService studyPlanService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileContentService fileContentService;

    /**
     * API để Frontend kích hoạt việc sinh Lộ trình học mới
     * URL: POST /api/study-plans/generate
     * Dữ liệu gửi lên: multipart/form-data với:
     *  - request: JSON (StudyPlanRequestDTO)
     *  - lectureFile: file bài giảng (tùy chọn)
     */
    @PostMapping(
            value = "/generate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<StudyPlanDTO> generateStudyPlan(
            @Valid @RequestPart("request") StudyPlanRequestDTO requestDTO,
            @RequestPart(value = "lectureFile", required = false) MultipartFile lectureFile
    ) {

        // Đọc nội dung file nếu có
        String lectureText = fileContentService.extractText(lectureFile);

        // Gọi Service (đã chỉnh để nhận thêm lectureText)
        StudyPlan newStudyPlan = studyPlanService.createStudyPlan(requestDTO, lectureText);

        // Map Entity -> DTO
        StudyPlanDTO responseDTO = modelMapper.map(newStudyPlan, StudyPlanDTO.class);

        return ResponseEntity.ok(responseDTO);
    }
}
