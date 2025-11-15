package com.example.smartlearning.service;

import com.example.smartlearning.dto.StudyPlanRequestDTO;
import com.example.smartlearning.model.StudyPlan;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.StudyPlanRepository;
import com.example.smartlearning.repository.UserSubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartlearning.service.LearningLogService;

@Service
public class StudyPlanService {

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private UserSubjectRepository userSubjectRepository;

    @Autowired
    private AiGenerationService aiGenerationService;

    @Autowired
    private LearningLogService learningLogService;

    @Transactional
    public StudyPlan createStudyPlan(StudyPlanRequestDTO requestDTO, String lectureText) {

        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));

        String aiContent = aiGenerationService.generateStudyPlan(
                userSubject.getUser(),
                userSubject.getSubject(),
                requestDTO.getCustomPrompt(),
                lectureText
        );

        StudyPlan newStudyPlan = new StudyPlan();
        newStudyPlan.setUserSubject(userSubject);
        newStudyPlan.setPlanContent(aiContent);
        newStudyPlan.setAiModelUsed("gemini-pro");

        StudyPlan savedPlan = studyPlanRepository.save(newStudyPlan);

        try {
            learningLogService.logActivity(
                    userSubject.getUser(),
                    userSubject,
                    "PLAN_GENERATED",
                    "Đã tạo lộ trình: " + userSubject.getSubject().getSubjectName(),
                    0
            );
        } catch (Exception e) {
            System.err.println("Lỗi ghi log (Plan): " + e.getMessage());
        }

        return savedPlan;
    }
}