package com.example.smartlearning.service;

import com.example.smartlearning.dto.QuestionFeedbackDTO;
import com.example.smartlearning.dto.QuizDetailDTO;
import com.example.smartlearning.dto.QuizInfoDTO;
import com.example.smartlearning.dto.QuizQuestionDetailDTO;
import com.example.smartlearning.dto.QuizRequestDTO;
import com.example.smartlearning.dto.SubmitQuizRequestDTO;
import com.example.smartlearning.dto.SubmitQuizResponseDTO;
import com.example.smartlearning.dto.ai.AiQuizQuestionDTO;
import com.example.smartlearning.model.Quiz;
import com.example.smartlearning.model.QuizQuestions;
import com.example.smartlearning.model.Subject;
import com.example.smartlearning.model.Topic;
import com.example.smartlearning.model.UserQuestionAnswer;
import com.example.smartlearning.model.UserQuizAttempt;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.QuizQuestionsRepository;
import com.example.smartlearning.repository.QuizRepository;
import com.example.smartlearning.repository.TopicRepository;
import com.example.smartlearning.repository.UserQuestionAnswerRepository;
import com.example.smartlearning.repository.UserQuizAttemptRepository;
import com.example.smartlearning.repository.UserSubjectRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private QuizQuestionsRepository quizQuestionsRepository;

    @Autowired
    private UserQuizAttemptRepository attemptRepository;

    @Autowired
    private UserQuestionAnswerRepository answerRepository;

    // --- ĐÃ THÊM REPO MỚI ---
    @Autowired
    private TopicRepository topicRepository;
    // --- KẾT THÚC THÊM ---


    @Transactional
    public Quiz createQuiz(QuizRequestDTO requestDTO, String lectureText) {

        // 1. Lấy thông tin User và Subject
        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));

        // --- ĐÃ THÊM ---
        // Lấy Subject entity để dùng cho việc tìm/tạo Topic
        Subject subject = userSubject.getSubject();
        // --- KẾT THÚC THÊM ---

        // 2. Gọi Service AI
        String jsonResponse = aiGenerationService.generateQuiz(
                userSubject.getUser(),
                subject, // <-- Đã SỬA: Dùng 'subject' thay vì 'userSubject.getSubject()'
                requestDTO.getTopic(),
                requestDTO.getNumberOfQuestions(),
                lectureText
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
            newQuiz.setTitle(requestDTO.getTopic() != null ? "Quiz: " + requestDTO.getTopic() : "Quiz " + subject.getSubjectName());
            newQuiz.setAiModelUsed("gemini-1.5-flash");
            newQuiz.setGeneratedAt(LocalDateTime.now());

            List<QuizQuestions> questionEntities = new ArrayList<>();
            for (AiQuizQuestionDTO aiQ : aiQuestions) {
                QuizQuestions q = new QuizQuestions();
                q.setQuiz(newQuiz);
                q.setQuestionText(aiQ.getQuestionText());
                q.setCorrectAnswer(aiQ.getCorrectAnswer());
                q.setExplanation(aiQ.getExplanation());
                q.setOptions(objectMapper.writeValueAsString(aiQ.getOptions()));

                // --- BẮT ĐẦU LOGIC CẬP NHẬT TOPIC ---
                // (Thay thế cho comment cũ của bạn)
                String topicName = aiQ.getTopicName();
                if (topicName != null && !topicName.isBlank()) {

                    // Tìm xem Topic này đã có trong DB của môn học này chưa
                    Topic topic = topicRepository.findByTopicNameAndSubject(topicName, subject)
                            .orElseGet(() -> {
                                // Nếu chưa có, tạo Topic mới
                                Topic newTopic = new Topic();
                                newTopic.setSubject(subject);
                                newTopic.setTopicName(topicName);
                                return topicRepository.save(newTopic);
                            });

                    // Gắn câu hỏi này với Topic đó
                    q.setTopic(topic);
                }
                // --- KẾT THÚC LOGIC CẬP NHẬT TOPIC ---

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


    /**
     * XỬ LÝ NỘP BÀI QUIZ
     * (Giữ nguyên logic của bạn từ Bước 1)
     */
    @Transactional
    public SubmitQuizResponseDTO submitQuiz(SubmitQuizRequestDTO submitDTO) {

        UserSubject userSubject = userSubjectRepository.findById(submitDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));
        Quiz quiz = quizRepository.findById(submitDTO.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        List<QuizQuestions> questions = quizQuestionsRepository.findByQuiz_QuizId(submitDTO.getQuizId());

        int totalQuestions = questions.size();
        int correctCount = 0;

        UserQuizAttempt attempt = new UserQuizAttempt();
        attempt.setUserSubject(userSubject);
        attempt.setQuiz(quiz);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setScore(0);

        UserQuizAttempt savedAttempt = attemptRepository.save(attempt);

        List<UserQuestionAnswer> userAnswersToSave = new ArrayList<>();
        List<QuestionFeedbackDTO> feedbackList = new ArrayList<>();
        Map<Integer, String> userSubmittedAnswers = submitDTO.getAnswers();

        for (QuizQuestions q : questions) {
            Integer qId = q.getQuestionId();
            String selectedAnswer = userSubmittedAnswers.getOrDefault(qId, "");
            String correctAnswer = q.getCorrectAnswer();
            String explanation = q.getExplanation();

            boolean isCorrect = selectedAnswer.equals(correctAnswer);
            if (isCorrect) {
                correctCount++;
            }

            UserQuestionAnswer uqa = new UserQuestionAnswer();
            uqa.setAttempt(savedAttempt);
            uqa.setQuestion(q);
            uqa.setSelectedAnswer(selectedAnswer);
            uqa.setCorrect(isCorrect);
            userAnswersToSave.add(uqa);
            System.out.println(isCorrect);
            feedbackList.add(new QuestionFeedbackDTO(qId, selectedAnswer, correctAnswer, isCorrect, explanation));
        }

        answerRepository.saveAll(userAnswersToSave);

        double finalScore = (totalQuestions > 0) ? ((double) correctCount / totalQuestions) * 10 : 0;
        if (totalQuestions > 0) {
            double passPercentage = (double) finalScore / totalQuestions;

            if (passPercentage >= 0.5) {
                int currentProgress = userSubject.getProgressPercentage();
                int newProgress = Math.min(currentProgress + 10, 100);

                userSubject.setProgressPercentage(newProgress);
                userSubjectRepository.save(userSubject);
            }
        }
        savedAttempt.setScore(finalScore);
        attemptRepository.save(savedAttempt);

        updateUserSubjectScore(userSubject, finalScore);

        learningLogService.logActivity(
                userSubject.getUser(),
                userSubject,
                "QUIZ_TAKEN",
                String.format("Hoàn thành quiz '%s' với điểm %.1f/10", quiz.getTitle(), finalScore),
                0
        );

        SubmitQuizResponseDTO response = new SubmitQuizResponseDTO();
        response.setAttemptId(savedAttempt.getAttemptId());
        response.setScore(finalScore);
        response.setTotalQuestions(totalQuestions);
        response.setCorrectCount(correctCount);
        response.setFeedback(feedbackList);

        return response;
    }

    private void updateUserSubjectScore(UserSubject userSubject, double newScore) {
        Double currentScore = userSubject.getCurrentScore();
        if (currentScore == null || currentScore == 0) {
            userSubject.setCurrentScore(newScore);
        } else {
            double averageScore = (currentScore + newScore) / 2.0;
            userSubject.setCurrentScore(averageScore);
        }
        userSubjectRepository.save(userSubject);
    }

    public void createQuizDTO(QuizInfoDTO quiz, UserSubject usersubject)
    {
        List<QuizQuestions> questions = quizQuestionsRepository.findByQuiz_QuizId(quiz.getQuizId());
        Quiz Q = quizRepository.findById(quiz.getQuizId()).orElse(null);

        int totalQuestions = questions.size();
        quiz.setQuestionCount(totalQuestions);
        UserQuizAttempt lastAttempt = attemptRepository
        	    .findTopByUserSubject_IdAndQuiz_QuizIdOrderByAttemptTimeDesc(
        	        usersubject.getId(), quiz.getQuizId()
        	    )
        	    .orElse(null);

        	if (lastAttempt != null) {
        	    quiz.setLastAttemptScore(lastAttempt.getScore());
        	    quiz.setIncorrectCount(answerRepository.countIncorrectByAttemptId(lastAttempt.getAttemptId()));
        	}

    }
}