//package com.example.smartlearning.service;
//
//import com.example.smartlearning.dto.QuizDetailDTO;
//import com.example.smartlearning.dto.QuizQuestionDetailDTO;
//import com.example.smartlearning.dto.QuizRequestDTO;
//import com.example.smartlearning.dto.ai.AiQuizQuestionDTO;
//import com.example.smartlearning.model.Quiz;
//import com.example.smartlearning.model.QuizQuestions;
//import com.example.smartlearning.model.UserSubject;
//import com.example.smartlearning.repository.QuizRepository;
//import com.example.smartlearning.repository.UserSubjectRepository;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//// MỚI: Import service log
//import com.example.smartlearning.service.LearningLogService;
//
//@Service
//public class QuizService {
//
//    @Autowired
//    private QuizRepository quizRepository;
//
//    @Autowired
//    private UserSubjectRepository userSubjectRepository;
//
//    @Autowired
//    private AiGenerationService aiGenerationService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    // MỚI: Tiêm (inject) LearningLogService
//    @Autowired
//    private LearningLogService learningLogService;
//
//    @Transactional
//    public Quiz createQuiz(QuizRequestDTO requestDTO) {
//
//        // 1. Lấy thông tin User và Subject
//        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
//                .orElseThrow(() -> new RuntimeException("UserSubject not found"));
//
//        // 2. Gọi Service AI để sinh nội dung (một chuỗi JSON)
//        String jsonResponse = aiGenerationService.generateQuiz(
//                userSubject.getUser(),
//                userSubject.getSubject(),
//                requestDTO.getTopic(),
//                requestDTO.getNumberOfQuestions()
//        );
//
//        // 3. Phân tích (Parse) chuỗi JSON
//        try {
//            List<AiQuizQuestionDTO> aiQuestions = objectMapper.readValue(
//                    jsonResponse,
//                    new TypeReference<List<AiQuizQuestionDTO>>() {}
//            );
//
//            // 4. Tạo các Entity
//            Quiz newQuiz = new Quiz();
//            newQuiz.setUserSubject(userSubject);
//            newQuiz.setTitle(requestDTO.getTopic() != null ? requestDTO.getTopic() : "Quiz " + userSubject.getSubject().getSubjectName());
//            newQuiz.setAiModelUsed("gpt-4o-mini-mock-json");
//
//            List<QuizQuestions> questionEntities = new ArrayList<>();
//            for (AiQuizQuestionDTO aiQ : aiQuestions) {
//                QuizQuestions q = new QuizQuestions();
//                q.setQuiz(newQuiz);
//                q.setQuestionText(aiQ.getQuestionText());
//                q.setCorrectAnswer(aiQ.getCorrectAnswer());
//                q.setExplanation(aiQ.getExplanation());
//                q.setOptions(objectMapper.writeValueAsString(aiQ.getOptions()));
//                questionEntities.add(q);
//            }
//
//            newQuiz.setQuestions(questionEntities);
//
//            // 5. Lưu vào DB
//            // MỚI: Gán vào biến 'savedQuiz'
//            Quiz savedQuiz = quizRepository.save(newQuiz);
//
//            // 6. GHI LOG (MỚI)
//            try {
//                learningLogService.logActivity(
//                        userSubject.getUser(),
//                        userSubject,
//                        "QUIZ_GENERATED",
//                        "Đã tạo quiz: " + savedQuiz.getTitle(),
//                        0
//                );
//            } catch (Exception e) {
//                System.err.println("Lỗi ghi log (Quiz): " + e.getMessage());
//            }
//
//            return savedQuiz; // Trả về entity đã được lưu
//
//        } catch (Exception e) {
//            System.err.println("Không thể parse JSON từ AI (Quiz): " + e.getMessage());
//            System.err.println("JSON nhận được: " + jsonResponse);
//            throw new RuntimeException("Lỗi xử lý dữ liệu từ AI", e);
//        }
//    }
//    /**
//     * Lấy chi tiết đầy đủ của một bộ Quiz (bao gồm câu hỏi & đáp án)
//     * @param quizId ID của bộ Quiz
//     * @return QuizDetailDTO
//     */
//    @Transactional(readOnly = true)
//    public QuizDetailDTO getQuizDetails(Integer quizId) {
//        // 1. Lấy Quiz (nó sẽ tự động tải các câu hỏi vì Eager loading
//        // hoặc bạn có thể fetch)
//        Quiz quiz = quizRepository.findById(quizId)
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy Quiz: " + quizId));
//
//        // 2. Map Quiz (Entity) -> QuizDetailDTO
//        QuizDetailDTO quizDTO = new QuizDetailDTO();
//        quizDTO.setQuizId(quiz.getQuizId());
//        quizDTO.setTitle(quiz.getTitle());
//        quizDTO.setGeneratedAt(quiz.getGeneratedAt());
//
//        // 3. Map thủ công các câu hỏi (để xử lý JSON 'options')
//        List<QuizQuestionDetailDTO> questionDTOs = quiz.getQuestions().stream()
//                .map(questionEntity -> {
//                    QuizQuestionDetailDTO qDto = new QuizQuestionDetailDTO();
//                    qDto.setQuestionId(questionEntity.getQuestionId());
//                    qDto.setQuestionText(questionEntity.getQuestionText());
//                    qDto.setCorrectAnswer(questionEntity.getCorrectAnswer());
//                    qDto.setExplanation(questionEntity.getExplanation());
//
//                    // Chuyển chuỗi JSON 'options' -> Object (Map)
//                    try {
//                        Object optionsObject = objectMapper.readValue(
//                                questionEntity.getOptions(), Object.class
//                        );
//                        qDto.setOptions(optionsObject);
//                    } catch (Exception e) {
//                        qDto.setOptions(null); // Hoặc một Map rỗng
//                    }
//                    return qDto;
//                })
//                .collect(Collectors.toList());
//
//        quizDTO.setQuestions(questionDTOs);
//        return quizDTO;
//    }
//}
package com.example.smartlearning.service;

import com.example.smartlearning.dto.QuizDetailDTO;
import com.example.smartlearning.dto.QuizQuestionDetailDTO;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private LearningLogService learningLogService;

    @Transactional
    public Quiz createQuiz(QuizRequestDTO requestDTO) {

        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));

        String jsonResponse = aiGenerationService.generateQuiz(
                userSubject.getUser(),
                userSubject.getSubject(),
                requestDTO.getTopic(),
                requestDTO.getNumberOfQuestions()
        );

        try {
            List<AiQuizQuestionDTO> aiQuestions = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<List<AiQuizQuestionDTO>>() {}
            );

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

            Quiz savedQuiz = quizRepository.save(newQuiz);

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

            return savedQuiz;

        } catch (Exception e) {
            System.err.println("Không thể parse JSON từ AI (Quiz): " + e.getMessage());
            System.err.println("JSON nhận được: " + jsonResponse);
            throw new RuntimeException("Lỗi xử lý dữ liệu từ AI", e);
        }
    }

    @Transactional(readOnly = true)
    public QuizDetailDTO getQuizDetails(Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Quiz: " + quizId));

        QuizDetailDTO quizDTO = new QuizDetailDTO();
        quizDTO.setQuizId(quiz.getQuizId());
        quizDTO.setTitle(quiz.getTitle());
        quizDTO.setGeneratedAt(quiz.getGeneratedAt());

        List<QuizQuestionDetailDTO> questionDTOs = quiz.getQuestions().stream()
                .map(questionEntity -> {
                    QuizQuestionDetailDTO qDto = new QuizQuestionDetailDTO();
                    qDto.setQuestionId(questionEntity.getQuestionId());
                    qDto.setQuestionText(questionEntity.getQuestionText());
                    qDto.setCorrectAnswer(questionEntity.getCorrectAnswer());
                    qDto.setExplanation(questionEntity.getExplanation());

                    try {
                        Object optionsObject = objectMapper.readValue(
                                questionEntity.getOptions(), Object.class
                        );
                        qDto.setOptions(optionsObject);
                    } catch (Exception e) {
                        qDto.setOptions(null);
                    }
                    return qDto;
                })
                .collect(Collectors.toList());

        quizDTO.setQuestions(questionDTOs);
        return quizDTO;
    }

    @Transactional
    public void submitQuiz(Integer quizId, Integer userId, Integer durationInMinutes, Integer score) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        UserSubject userSubject = quiz.getUserSubject();

        if (!userSubject.getUser().getUserId().equals(userId)) {
            throw new SecurityException("User not authorized for this quiz");
        }

        try {
            learningLogService.logActivity(
                    userSubject.getUser(),
                    userSubject,
                    "QUIZ_COMPLETED",
                    String.format("Hoàn thành quiz: %s (Điểm: %d)", quiz.getTitle(), score),
                    durationInMinutes
            );
        } catch (Exception e) {
            System.err.println("Lỗi ghi log (Quiz Submit): " + e.getMessage());
        }
    }
}