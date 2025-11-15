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
    public Quiz createQuiz(QuizRequestDTO requestDTO, String lectureText) {

        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));

        String jsonResponse = aiGenerationService.generateQuiz(
                userSubject.getUser(),
                userSubject.getSubject(),
                requestDTO.getTopic(),
                requestDTO.getNumberOfQuestions(),
                lectureText
        );

        if ("ERROR_TOPIC_NOT_FOUND".equals(jsonResponse)) {
            throw new RuntimeException("Không tìm thấy chủ đề '" + requestDTO.getTopic() + "' trong tệp đã tải lên. Vui lòng thử lại với chủ đề khác hoặc để trống.");
        }

        try {
            List<AiQuizQuestionDTO> aiQuestions = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<List<AiQuizQuestionDTO>>() {}
            );

            Quiz newQuiz = new Quiz();
            newQuiz.setUserSubject(userSubject);
            newQuiz.setTitle(requestDTO.getTopic() != null ? requestDTO.getTopic() : "Quiz " + userSubject.getSubject().getSubjectName());
            newQuiz.setAiModelUsed(aiGenerationService.getAiModelUsed());
            newQuiz.setGeneratedAt(LocalDateTime.now());

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
        quizDTO.setUserSubjectId(quiz.getUserSubject().getId());

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

        int totalQuestions = quiz.getQuestions().size();
        if (totalQuestions > 0) {
            double passPercentage = (double) score / totalQuestions;

            if (passPercentage >= 0.5) {
                int currentProgress = userSubject.getProgressPercentage();
                int newProgress = Math.min(currentProgress + 10, 100);

                userSubject.setProgressPercentage(newProgress);
                userSubjectRepository.save(userSubject);
            }
        }
    }
}