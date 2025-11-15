package com.example.smartlearning.controller;

import com.example.smartlearning.dto.QuizDTO;
import com.example.smartlearning.dto.QuizDetailDTO;
import com.example.smartlearning.dto.QuizQuestionDTO;
import com.example.smartlearning.dto.QuizRequestDTO;
import com.example.smartlearning.model.Quiz;
import com.example.smartlearning.service.FileContentService;
import com.example.smartlearning.service.QuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper; // Cần để xử lý JSON "options"

    @Autowired
    private FileContentService fileContentService;

    /**
     * API để Frontend kích hoạt việc sinh Quiz mới
     * URL: POST /api/quizzes/generate
     * Dữ liệu: multipart/form-data với:
     *  - request: JSON (QuizRequestDTO)
     *  - lectureFile: file bài giảng (tùy chọn)
     */
    @PostMapping(
            value = "/generate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<QuizDTO> generateQuiz(
            @Valid @RequestPart("request") QuizRequestDTO requestDTO,
            @RequestPart(value = "lectureFile", required = false) MultipartFile lectureFile
    ) {

        String lectureText = fileContentService.extractText(lectureFile);

        // 1. Gọi Service (đã chứa logic gọi AI, parse JSON, và lưu DB)
        Quiz newQuiz = quizService.createQuiz(requestDTO, lectureText);

        // 2. Map Entity -> DTO
        QuizDTO responseDTO = mapQuizToQuizDTO(newQuiz);

        // 3. Trả DTO về cho Frontend
        return ResponseEntity.ok(responseDTO);
    }

    // --- Helper Method (Phương thức hỗ trợ) ---

    private QuizDTO mapQuizToQuizDTO(Quiz quiz) {
        QuizDTO dto = modelMapper.map(quiz, QuizDTO.class);

        List<QuizQuestionDTO> questionDTOs = new ArrayList<>();

        quiz.getQuestions().forEach(questionEntity -> {
            QuizQuestionDTO qDto = modelMapper.map(questionEntity, QuizQuestionDTO.class);

            try {
                String optionsJsonString = questionEntity.getOptions();
                Object optionsObject = objectMapper.readValue(optionsJsonString, Object.class);
                qDto.setOptions(optionsObject);
            } catch (Exception e) {
                qDto.setOptions(null);
            }
            questionDTOs.add(qDto);
        });

        dto.setQuestions(questionDTOs);
        return dto;
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDetailDTO> getQuizById(@PathVariable Integer quizId) {

        QuizDetailDTO quizDetails = quizService.getQuizDetails(quizId);

        return ResponseEntity.ok(quizDetails);
    }
}
