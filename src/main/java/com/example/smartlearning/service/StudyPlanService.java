package com.example.smartlearning.service;

import com.example.smartlearning.dto.StudyPlanRequestDTO;
import com.example.smartlearning.model.StudyPlan;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.StudyPlanRepository;
import com.example.smartlearning.repository.UserSubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// MỚI: Import service log
import com.example.smartlearning.service.LearningLogService;

@Service
public class StudyPlanService {

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private UserSubjectRepository userSubjectRepository;

    @Autowired
    private AiGenerationService aiGenerationService;

    // MỚI: Tiêm (inject) LearningLogService
    @Autowired
    private LearningLogService learningLogService;

    /**
     * Logic nghiệp vụ chính: Tạo và lưu một lộ trình học

     * @param requestDTO dữ liệu người dùng gửi
     * @param lectureText nội dung file bài giảng (có thể null nếu không upload)
     */
    @Transactional
    public StudyPlan createStudyPlan(StudyPlanRequestDTO requestDTO, String lectureText) {

        // 1. Lấy thông tin User và Subject từ DB
        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));

        // 2. Gọi Service AI để sinh nội dung, có thêm lectureText
        String aiContent = aiGenerationService.generateStudyPlan(
                userSubject.getUser(),
                userSubject.getSubject(),
                requestDTO.getCustomPrompt(),
                lectureText
        );

        // 3. Tạo Entity mới
        StudyPlan newStudyPlan = new StudyPlan();
        newStudyPlan.setUserSubject(userSubject);
        newStudyPlan.setPlanContent(aiContent);
        newStudyPlan.setAiModelUsed("gpt-4o-mini-mock");

        // 4. Lưu vào Database
        StudyPlan savedPlan = studyPlanRepository.save(newStudyPlan);

        // 5. GHI LOG (MỚI)
        try {
            learningLogService.logActivity(
                    userSubject.getUser(),
                    userSubject,
                    "PLAN_GENERATED",
                    "Đã tạo lộ trình: " + userSubject.getSubject().getSubjectName(),
                    0 // Hoạt động tạo không tốn thời gian
            );
        } catch (Exception e) {
            System.err.println("Lỗi ghi log (Plan): " + e.getMessage());
        }

        return savedPlan;
    }
}
