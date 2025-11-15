// Đặt tại: src/main/java/com/example/smartlearning/service/SubjectContentService.java
package com.example.smartlearning.service;

import com.example.smartlearning.dto.FlashcardSetInfoDTO;
import com.example.smartlearning.dto.QuizInfoDTO;
import com.example.smartlearning.dto.StudyPlanDTO;
import com.example.smartlearning.dto.SubjectContentDTO;
import com.example.smartlearning.model.FlashcardSet;
import com.example.smartlearning.model.Quiz;
import com.example.smartlearning.model.StudyPlan;
import com.example.smartlearning.model.UserQuizAttempt; // <-- THÊM MỚI
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.*; // Import tất cả repo
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubjectContentService {

    @Autowired
    private UserSubjectRepository userSubjectRepository;

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private ModelMapper modelMapper;

    // --- BẮT ĐẦU THÊM REPO ---
    @Autowired
    private UserQuizAttemptRepository attemptRepository;

    @Autowired
    private QuizQuestionsRepository quizQuestionsRepository;
    // --- KẾT THÚC THÊM REPO ---


    @Transactional(readOnly = true)
    public SubjectContentDTO getSubjectContent(Integer userSubjectId) {

        UserSubject us = userSubjectRepository.findById(userSubjectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học đã đăng ký: " + userSubjectId));

        List<StudyPlan> plans = studyPlanRepository.findByUserSubjectId(userSubjectId);
        List<Quiz> quizzes = quizRepository.findByUserSubjectId(userSubjectId);
        List<FlashcardSet> sets = flashcardSetRepository.findByUserSubjectId(userSubjectId);


        List<StudyPlanDTO> planDTOs = plans.stream()
                .map(p -> modelMapper.map(p, StudyPlanDTO.class))
                .collect(Collectors.toList());

        // --- BẮT ĐẦU SỬA ĐỔI LOGIC QUIZ ---
        List<QuizInfoDTO> quizDTOs = quizzes.stream()
                .map(q -> {
                    QuizInfoDTO dto = modelMapper.map(q, QuizInfoDTO.class);

                    // Lấy lại totalQuestions thủ công nếu ModelMapper chưa tự động map
                    if (dto.getQuestionCount() == 0) {
                        dto.setQuestionCount(quizQuestionsRepository.countByQuiz_QuizId(q.getQuizId()));
                    }

                    // 1. Tìm lần làm bài gần nhất
                    Optional<UserQuizAttempt> lastAttempt = attemptRepository.findTopByUserSubject_IdAndQuiz_QuizIdOrderByAttemptTimeDesc(
                            userSubjectId, q.getQuizId()
                    );

                    if (lastAttempt.isPresent()) {
                        UserQuizAttempt attempt = lastAttempt.get();
                        dto.setLastAttemptScore(attempt.getScore());
                        dto.setLastAttemptTime(attempt.getAttemptTime());

                        // 2. Tính số câu sai (total - correct)
                        int totalQuestions = dto.getQuestionCount();

                        if (totalQuestions > 0) {
                            // Tính số câu đúng (Score = (correct / total) * 10)
                            int correctCount = (int) Math.round(attempt.getScore() * totalQuestions / 10.0);
                            dto.setIncorrectCount(totalQuestions - correctCount);
                        } else {
                            dto.setIncorrectCount(0);
                        }

                    } else {
                        dto.setIncorrectCount(null); // Chưa làm bài
                    }

                    return dto;
                })
                .collect(Collectors.toList());
        // --- KẾT THÚC SỬA ĐỔI LOGIC QUIZ ---

        List<FlashcardSetInfoDTO> setDTOs = sets.stream()
                .map(s -> modelMapper.map(s, FlashcardSetInfoDTO.class))
                .collect(Collectors.toList());

        SubjectContentDTO contentDTO = new SubjectContentDTO();
        contentDTO.setUserSubjectId(us.getId());
        contentDTO.setSubjectName(us.getSubject().getSubjectName());
        contentDTO.setSubjectDescription(us.getSubject().getDescription());
        contentDTO.setProgressPercentage(us.getProgressPercentage());

        contentDTO.setStudyPlans(planDTOs);
        contentDTO.setQuizzes(quizDTOs);
        contentDTO.setFlashcardSets(setDTOs);

        return contentDTO;
    }
}