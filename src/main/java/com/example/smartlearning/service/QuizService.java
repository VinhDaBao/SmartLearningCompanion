package com.example.smartlearning.service;

import com.example.smartlearning.dto.QuizRequestDTO;
import com.example.smartlearning.dto.ai.AiQuizQuestionDTO;
import com.example.smartlearning.model.Quiz;
import com.example.smartlearning.model.QuizQuestions;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.QuizRepository;
import com.example.smartlearning.repository.UserSubjectRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// MỚI: Import service log
import com.example.smartlearning.service.LearningLogService;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserSubjectRepository userSubjectRepository;

    @Autowired
    private AiGenerationService aiGenerationService;

    @Autowired
    private ObjectMapper objectMapper;

    // MỚI: Tiêm (inject) LearningLogService
    @Autowired
    private LearningLogService learningLogService;

    @Transactional
    public Quiz createQuiz(QuizRequestDTO requestDTO) {

        // 1. Lấy thông tin User và Subject
        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));

        // 2. Gọi Service AI để sinh nội dung (một chuỗi JSON)
        String jsonResponse = aiGenerationService.generateQuiz(
                userSubject.getUser(),
                userSubject.getSubject(),
                requestDTO.getTopic(),
                requestDTO.getNumberOfQuestions()
        );

        // 3. Phân tích (Parse) chuỗi JSON
        try {
            List<AiQuizQuestionDTO> aiQuestions = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<List<AiQuizQuestionDTO>>() {}
            );

            // 4. Tạo các Entity
            Quiz newQuiz = new Quiz();
            newQuiz.setUserSubject(userSubject);
            newQuiz.setTitle(requestDTO.getTopic() != null ? requestDTO.getTopic() : "Quiz " + userSubject.getSubject().getSubjectName());
            newQuiz.setAiModelUsed("gpt-4o-mini-mock-json");

            List<QuizQuestions> questionEntities = new ArrayList<>();
            for (AiQuizQuestionDTO aiQ : aiQuestions) {
                QuizQuestions q = new QuizQuestions();
                q.setQuiz(newQuiz);
                q.setQuestionText(aiQ.getQuestionText());
                q.setCorrectAnswer(aiQ.getCorrectAnswer());
                q.setExplanation(aiQ.getExplanation());
                q.setOptions(objectMapper.writeValueAsString(aiQ.getOptions()));
                questionEntities.add(q);
            }

            newQuiz.setQuestions(questionEntities);

            // 5. Lưu vào DB
            // MỚI: Gán vào biến 'savedQuiz'
            Quiz savedQuiz = quizRepository.save(newQuiz);

            // 6. GHI LOG (MỚI)
            try {
                learningLogService.logActivity(
                        userSubject.getUser(),
                        userSubject,
                        "QUIZ_GENERATED",
                        "Đã tạo quiz: " + savedQuiz.getTitle(),
                        0
                );
            } catch (Exception e) {
                System.err.println("Lỗi ghi log (Quiz): " + e.getMessage());
            }

            return savedQuiz; // Trả về entity đã được lưu

        } catch (Exception e) {
            System.err.println("Không thể parse JSON từ AI (Quiz): " + e.getMessage());
            System.err.println("JSON nhận được: " + jsonResponse);
            throw new RuntimeException("Lỗi xử lý dữ liệu từ AI", e);
        }
    }
}