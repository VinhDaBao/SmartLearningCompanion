// Đặt tại: src/main/java/com/example/smartlearning/service/AdaptiveLearningService.java
package com.example.smartlearning.service;

import com.example.smartlearning.dto.AdaptiveRecommendationDTO;
import com.example.smartlearning.model.SubjectContent;
import com.example.smartlearning.model.Topic;
import com.example.smartlearning.model.User;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.SubjectContentRepository;
import com.example.smartlearning.repository.UserQuestionAnswerRepository;
import com.example.smartlearning.repository.UserQuizAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdaptiveLearningService {

    @Autowired private UserQuestionAnswerRepository answerRepo;
    @Autowired private UserQuizAttemptRepository attemptRepo;
    @Autowired private SubjectContentRepository contentRepo;
    @Autowired private AiGenerationService aiService;

    @Transactional(readOnly = true)
    public AdaptiveRecommendationDTO getAdaptiveRecommendation(UserSubject userSubject) {

        long startTime = System.currentTimeMillis(); // Bắt đầu tính tổng thời gian
        User user = userSubject.getUser();

        // 1. Lấy phong cách học (với kiểm tra null)
        String learningStyle = user.getLearningStyle();
        if (learningStyle == null || learningStyle.isBlank()) {
            learningStyle = "Reading/Writing";
        }

        // 2. KIỂM TRA TỔNG SỐ LẦN THỬ
        long totalAttempts = attemptRepo.countByUserSubject_Id(userSubject.getId());
        if (totalAttempts == 0) {
            long aiStart = System.currentTimeMillis();
            String welcomeText = aiService.generateRecommendationText(
                    learningStyle, null, null, "NO_ATTEMPTS"
            );
            long endTime = System.currentTimeMillis();
            System.out.println("ADAPTIVE_DIAGNOSTIC: THỜI GIAN GỌI AI (NO_ATTEMPTS): " + (endTime - aiStart) + "ms");
            return new AdaptiveRecommendationDTO(welcomeText, null, false);
        }

        // --- BẮT ĐẦU ĐO THỜI GIAN CSDL ---
        long dbStart = System.currentTimeMillis();

        // 3. KIỂM TRA LỖI (Sử dụng count để tránh lỗi khi Topic/Quiz cũ)
        long totalErrors = answerRepo.countByAttempt_UserSubject_IdAndIsCorrect(userSubject.getId(), false);

        // 4. TÌM TOPIC YẾU NHẤT (Chỉ lấy 1 kết quả)
        List<Topic> weakestTopics = answerRepo.findWeakestTopicsByUserSubjectId(
                userSubject.getId(), PageRequest.of(0, 1)
        );

        long dbEnd = System.currentTimeMillis();
        System.out.println("ADAPTIVE_DIAGNOSTIC: THỜI GIAN TRUY VẤN CSDL: " + (dbEnd - dbStart) + "ms");
        // --- KẾT THÚC ĐO THỜI GIAN CSDL ---


        if (totalErrors == 0) {
            // Trường hợp: Đã làm quiz, nhưng không có lỗi nào (hoặc lỗi không được gắn tag)
            long aiStart = System.currentTimeMillis();
            String congratsText = aiService.generateRecommendationText(learningStyle, null, null, "PERFECT");
            long endTime = System.currentTimeMillis();
            System.out.println("ADAPTIVE_DIAGNOSTIC: THỜI GIAN GỌI AI (PERFECT): " + (endTime - aiStart) + "ms");
            return new AdaptiveRecommendationDTO(congratsText, null, false);
        }

        // 5. CHẨN ĐOÁN VÀ ĐỀ XUẤT
        Topic weakestTopic = weakestTopics.isEmpty() ? null : weakestTopics.get(0);

        // Xác định kịch bản
        String scenario;
        if (weakestTopic == null) {
            scenario = "GENERAL_ERROR";
        } else {
            // Tìm tài liệu
            String preferredType = learningStyle.equals("Visual") ? "VIDEO" : "TEXT";
            List<SubjectContent> materials = contentRepo.findByTopic_TopicIdAndContentType(weakestTopic.getTopicId(), preferredType);

            if (materials.isEmpty()) {
                materials = contentRepo.findByTopic_TopicId(weakestTopic.getTopicId());
            }

            if (materials.isEmpty()) {
                scenario = "WEAK_NO_MATERIAL";
            } else {
                scenario = "RECOMMEND";
            }

            // --- GỌI AI ĐỂ TẠO TEXT ---
            long aiStart = System.currentTimeMillis();
            SubjectContent recommendedContent = (scenario.equals("RECOMMEND")) ? materials.get(0) : null;

            String recommendationText = aiService.generateRecommendationText(
                    learningStyle,
                    weakestTopic,
                    recommendedContent,
                    scenario
            );

            long endTime = System.currentTimeMillis();
            System.out.println("ADAPTIVE_DIAGNOSTIC: THỜI GIAN GỌI AI (RECOMMEND/ERROR): " + (endTime - aiStart) + "ms");

            // Xây dựng kết quả cuối cùng
            System.out.println("ADAPTIVE_DIAGNOSTIC: TỔNG THỜI GIAN HÀM: " + (endTime - startTime) + "ms");
            return new AdaptiveRecommendationDTO(recommendationText, recommendedContent, scenario.equals("RECOMMEND"));
        }

        // Fallback for general error handling (no content needed for AI prompt here)
        long aiStart = System.currentTimeMillis();
        String recommendationText = aiService.generateRecommendationText(
                learningStyle,
                weakestTopic,
                null,
                scenario
        );
        long endTime = System.currentTimeMillis();
        System.out.println("ADAPTIVE_DIAGNOSTIC: THỜI GIAN GỌI AI (GENERAL_ERROR): " + (endTime - aiStart) + "ms");
        System.out.println("ADAPTIVE_DIAGNOSTIC: TỔNG THỜI GIAN HÀM: " + (endTime - startTime) + "ms");
        return new AdaptiveRecommendationDTO(recommendationText, null, false);
    }
}