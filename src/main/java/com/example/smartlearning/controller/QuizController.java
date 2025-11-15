package com.example.smartlearning.controller;

import com.example.smartlearning.dto.QuizDTO;
import com.example.smartlearning.dto.QuizQuestionDTO;
import com.example.smartlearning.dto.QuizRequestDTO;
import com.example.smartlearning.model.Quiz;
import com.example.smartlearning.service.QuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * API để Frontend kích hoạt việc sinh Quiz mới
     * URL: POST /api/quizzes/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<QuizDTO> generateQuiz(@Valid @RequestBody QuizRequestDTO requestDTO) {
        // 1. Gọi Service (đã chứa logic gọi AI, parse JSON, và lưu DB)
        Quiz newQuiz = quizService.createQuiz(requestDTO);

        // 2. Map Entity -> DTO
        // Chúng ta cần xử lý "options" thủ công một chút
        QuizDTO responseDTO = mapQuizToQuizDTO(newQuiz);

        // 3. Trả DTO về cho Frontend
        return ResponseEntity.ok(responseDTO);
    }

    // --- Helper Method (Phương thức hỗ trợ) ---

    /**
     * Chuyển đổi Quiz Entity sang Quiz DTO.
     * Lý do: Cần parse trường 'options' (từ JSON String sang JSON Object)
     * để JavaScript có thể đọc dễ dàng.
     */
    private QuizDTO mapQuizToQuizDTO(Quiz quiz) {
        // 1. Map các trường đơn giản (id, title...)
        QuizDTO dto = modelMapper.map(quiz, QuizDTO.class);

        List<QuizQuestionDTO> questionDTOs = new ArrayList<>();

        // 2. Map list câu hỏi và "sửa" lại trường options
        quiz.getQuestions().forEach(questionEntity -> {
            QuizQuestionDTO qDto = modelMapper.map(questionEntity, QuizQuestionDTO.class);

            // Đây là phần quan trọng!
            try {
                // Đọc chuỗi JSON (ví dụ: "{\"A\": \"...\"}")
                String optionsJsonString = questionEntity.getOptions();

                // Chuyển nó thành một đối tượng (Map)
                Object optionsObject = objectMapper.readValue(optionsJsonString, Object.class);

                // Gán đối tượng đó vào DTO
                qDto.setOptions(optionsObject);
            } catch (Exception e) {
                // Nếu lỗi, gán nó là null hoặc một đối tượng rỗng
                qDto.setOptions(null);
            }
            questionDTOs.add(qDto);
        });

        dto.setQuestions(questionDTOs);
        return dto;
    }
}