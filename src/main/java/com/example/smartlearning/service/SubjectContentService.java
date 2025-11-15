package com.example.smartlearning.service;

import com.example.smartlearning.dto.FlashcardSetInfoDTO;
import com.example.smartlearning.dto.QuizInfoDTO;
import com.example.smartlearning.dto.StudyPlanInfoDTO;
import com.example.smartlearning.dto.SubjectContentDTO;
import com.example.smartlearning.model.FlashcardSet;
import com.example.smartlearning.model.Quiz;
import com.example.smartlearning.model.StudyPlan;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.*; // Import tất cả repo
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    /**
     * Tổng hợp tất cả nội dung (Plans, Quizzes, Flashcards)
     * cho một môn học mà user đã đăng ký.
     */
    @Transactional(readOnly = true) // Chỉ đọc
    public SubjectContentDTO getSubjectContent(Integer userSubjectId) {

        // 1. Lấy đối tượng UserSubject (để lấy thông tin chung)
        UserSubject us = userSubjectRepository.findById(userSubjectId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học đã đăng ký: " + userSubjectId));

        // 2. Lấy danh sách Lộ trình (Entities)
        List<StudyPlan> plans = studyPlanRepository.findByUserSubjectId(userSubjectId);

        // 3. Lấy danh sách Quiz (Entities)
        List<Quiz> quizzes = quizRepository.findByUserSubjectId(userSubjectId);

        // 4. Lấy danh sách Flashcard (Entities)
        List<FlashcardSet> sets = flashcardSetRepository.findByUserSubjectId(userSubjectId);

        // 5. Chuyển đổi (Map) Entities -> DTOs
        List<StudyPlanInfoDTO> planDTOs = plans.stream()
                .map(p -> modelMapper.map(p, StudyPlanInfoDTO.class))
                .collect(Collectors.toList());

        List<QuizInfoDTO> quizDTOs = quizzes.stream()
                .map(q -> modelMapper.map(q, QuizInfoDTO.class))
                .collect(Collectors.toList());

        List<FlashcardSetInfoDTO> setDTOs = sets.stream()
                .map(s -> modelMapper.map(s, FlashcardSetInfoDTO.class))
                .collect(Collectors.toList());

        // 6. Tạo DTO container chính
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