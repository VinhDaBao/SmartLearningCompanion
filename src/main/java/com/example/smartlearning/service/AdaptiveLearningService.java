// Đặt tại: src/main/java/com/example/smartlearning/service/AdaptiveLearningService.java
package com.example.smartlearning.service;

import com.example.smartlearning.dto.AdaptiveRecommendationDTO;
import com.example.smartlearning.model.SubjectContent;
import com.example.smartlearning.model.Topic;
import com.example.smartlearning.model.User;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.SubjectContentRepository;
import com.example.smartlearning.repository.UserQuestionAnswerRepository;
import com.example.smartlearning.repository.UserQuizAttemptRepository; // <-- IMPORT MỚI
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdaptiveLearningService {

    @Autowired
    private UserQuestionAnswerRepository answerRepo;

    // --- THÊM REPO MỚI ---
    @Autowired
    private UserQuizAttemptRepository attemptRepo;
    // --- KẾT THÚC THÊM ---

    @Autowired
    private SubjectContentRepository contentRepo;

    @Autowired
    private AiGenerationService aiService;

    @Transactional(readOnly = true)
    public AdaptiveRecommendationDTO getAdaptiveRecommendation(UserSubject userSubject) {

        User user = userSubject.getUser();

        // 1. Lấy phong cách học (với kiểm tra null)
        String learningStyle = user.getLearningStyle();
        if (learningStyle == null || learningStyle.isBlank()) {
            learningStyle = "Reading/Writing"; // Mặc định
        }

        // --- BẮT ĐẦU SỬA LỖI LOGIC ---

        // 2. KIỂM TRA TRƯỜNG HỢP 1: SINH VIÊN CHƯA LÀM GÌ CẢ
        long totalAttempts = attemptRepo.countByUserSubject_Id(userSubject.getId());
        if (totalAttempts == 0) {
            // Đây là lỗi "chưa làm quiz hay tiến độ 0%"
            String welcomeText = aiService.generateRecommendationText(
                    learningStyle, null, null, "NO_ATTEMPTS"
            );
            return new AdaptiveRecommendationDTO(welcomeText, null, false);
        }

        // 3. KIỂM TRA TRƯỜNG HỢP 2: ĐÃ LÀM QUIZ, NHƯNG KHÔNG CÓ LỖI NÀO
        long totalErrors = answerRepo.countByAttempt_UserSubject_IdAndIsCorrect(userSubject.getId(), false);
        if (totalErrors == 0) {
            // Đây là trường hợp "làm đúng hết"
            String congratsText = aiService.generateRecommendationText(
                    learningStyle, null, null, "PERFECT"
            );
            return new AdaptiveRecommendationDTO(congratsText, null, false);
        }

        // 4. KIỂM TRA TRƯỜNG HỢP 3: CÓ LỖI, NHƯNG LÀ QUIZ CŨ (KHÔNG GẮN TOPIC)
        // (Chúng ta biết totalErrors > 0, nhưng query tìm topic lại rỗng)
        List<Topic> weakestTopics = answerRepo.findWeakestTopicsByUserSubjectId(
                userSubject.getId(), PageRequest.of(0, 1)
        );

        if (weakestTopics.isEmpty()) {
            // Đây là lỗi "làm sai hết quiz cũ"
            String generalErrorText = aiService.generateRecommendationText(
                    learningStyle, null, null, "GENERAL_ERROR"
            );
            return new AdaptiveRecommendationDTO(generalErrorText, null, false);
        }

        // 5. TRƯỜNG HỢP 4: CHUẨN (CÓ LỖI, CÓ TOPIC -> ĐỀ XUẤT)
        Topic weakestTopic = weakestTopics.get(0);

        // --- KẾT THÚC SỬA LỖI LOGIC ---

        // 6. Kê đơn: Tìm "thuốc" (tài liệu)
        String preferredType = "TEXT";
        if (learningStyle.equals("Visual")) {
            preferredType = "VIDEO";
        }

        List<SubjectContent> materials = contentRepo.findByTopic_TopicIdAndContentType(
                weakestTopic.getTopicId(), preferredType
        );

        if (materials.isEmpty()) {
            materials = contentRepo.findByTopic_TopicId(weakestTopic.getTopicId());
        }

        // 7. Nếu thư viện rỗng
        if (materials.isEmpty()) {
            String noMaterialText = aiService.generateRecommendationText(
                    learningStyle, weakestTopic, null, "WEAK_NO_MATERIAL"
            );
            return new AdaptiveRecommendationDTO(noMaterialText, null, false);
        }

        // 8. Đề xuất
        SubjectContent recommendedContent = materials.get(0);
        String recommendationText = aiService.generateRecommendationText(
                learningStyle, weakestTopic, recommendedContent, "RECOMMEND"
        );
        return new AdaptiveRecommendationDTO(recommendationText, recommendedContent, true);
    }
}