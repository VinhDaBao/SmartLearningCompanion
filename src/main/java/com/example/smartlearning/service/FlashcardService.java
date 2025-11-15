// Đặt tại: src/main/java/com/example/smartlearning/service/FlashcardService.java
package com.example.smartlearning.service;

import com.example.smartlearning.dto.*;
import com.example.smartlearning.dto.ai.AiFlashcardDTO;
import com.example.smartlearning.model.*;
import com.example.smartlearning.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // <-- THÊM IMPORT
import java.util.stream.Collectors;

@Service
public class FlashcardService {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;
    @Autowired
    private UserSubjectRepository userSubjectRepository;
    @Autowired
    private AiGenerationService aiGenerationService;
    @Autowired
    private ObjectMapper objectMapper;
    // @Autowired // (Đã xóa LearningLogService để tránh lỗi Vòng lặp)
    // private LearningLogService learningLogService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserFlashcardProgressRepository progressRepository;
    @Autowired
    private FlashcardRepository flashcardRepository;
    @Autowired
    private UserRepository userRepository;

    // (Hàm createFlashcardSet giữ nguyên)
    @Transactional
    public FlashcardSet createFlashcardSet(FlashcardRequestDTO requestDTO, String lectureText) {
        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));
        String jsonResponse = aiGenerationService.generateFlashcards(
                userSubject.getUser(), userSubject.getSubject(),
                requestDTO.getTopic(), requestDTO.getNumberOfCards(), lectureText
        );
        try {
            List<AiFlashcardDTO> aiCards = objectMapper.readValue(jsonResponse, new TypeReference<List<AiFlashcardDTO>>() {});
            FlashcardSet newSet = new FlashcardSet();
            newSet.setUserSubject(userSubject);
            newSet.setTitle(requestDTO.getTitle());
            newSet.setAiModelUsed("gemini-pro");
            List<Flashcard> cardEntities = aiCards.stream().map(aiCard -> {
                Flashcard card = new Flashcard();
                card.setFlashcardSet(newSet);
                card.setFrontText(aiCard.getFront());
                card.setBackText(aiCard.getBack());
                return card;
            }).collect(Collectors.toList());
            newSet.setFlashcards(cardEntities);
            FlashcardSet savedSet = flashcardSetRepository.save(newSet);
            // (Đã comment out learningLogService)
            return savedSet;
        } catch (Exception e) {
            System.err.println("Không thể parse JSON từ AI (Flashcard): " + e.getMessage());
            System.err.println("JSON nhận được: " + jsonResponse);
            throw new RuntimeException("Lỗi xử lý dữ liệu từ AI", e);
        }
    }

    // (Hàm getFlashcardSetDetails giữ nguyên)
    @Transactional(readOnly = true)
    public FlashcardSetDTO getFlashcardSetDetails(Integer setId) {
        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ Flashcard: " + setId));
        FlashcardSetDTO setDTO = modelMapper.map(flashcardSet, FlashcardSetDTO.class);
        setDTO.setUserSubjectId(flashcardSet.getUserSubject().getId());
        return setDTO;
    }

    // (Hàm reviewFlashcard giữ nguyên)
    @Transactional
    public void reviewFlashcard(User user, FlashcardReviewDTO reviewDTO) {
        Integer flashcardId = reviewDTO.getFlashcardId();
        boolean remembered = reviewDTO.getWasRemembered();
        Flashcard card = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new RuntimeException("Flashcard not found"));
        UserFlashcardProgress progress = progressRepository
                .findByUser_UserIdAndFlashcard_FlashcardId(user.getUserId(), flashcardId)
                .orElse(new UserFlashcardProgress(user, card));
        int currentBox = progress.getCurrentBox();
        if (remembered) { currentBox++; } else { currentBox = 1; }
        int daysToAdd = 0;
        switch (currentBox) {
            case 1: daysToAdd = 1; break;
            case 2: daysToAdd = 3; break;
            case 3: daysToAdd = 7; break;
            case 4: daysToAdd = 14; break;
            default: daysToAdd = 30; break;
        }
        progress.setCurrentBox(currentBox);
        progress.setNextReviewDate(LocalDateTime.now().plusDays(daysToAdd));
        progress.setLastReviewedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    // (Hàm getGlobalDueReviewCount giữ nguyên)
    public int getGlobalDueReviewCount(Integer userId) {
        return progressRepository.countDueReviews(userId, LocalDateTime.now());
    }

    // (Hàm getDueReviewFlashcards giữ nguyên)
    @Transactional(readOnly = true)
    public FlashcardSetDTO getDueReviewFlashcards(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Flashcard> dueCards = progressRepository.findDueFlashcards(userId, LocalDateTime.now());
        List<FlashcardDTO> cardDTOs = dueCards.stream()
                .map(card -> modelMapper.map(card, FlashcardDTO.class))
                .collect(Collectors.toList());
        FlashcardSetDTO virtualSet = new FlashcardSetDTO();
        virtualSet.setTitle("Phiên Ôn Tập (Tới Hạn)");
        virtualSet.setFlashcards(cardDTOs);
        virtualSet.setUserSubjectId(0);
        return virtualSet;
    }

    // =================================================================
    // === SỬA HÀM NÀY (THÊM LOGIC ĐẾM NGƯỢC) ===
    // =================================================================
    public FlashcardReviewSummaryDTO getReviewSummaryForSubject(Integer userSubjectId) {
        FlashcardReviewSummaryDTO summary = new FlashcardReviewSummaryDTO();

        // 1. Đếm thẻ tới hạn (giữ nguyên)
        summary.setDueCount(progressRepository.countDueReviewsForSubject(userSubjectId, LocalDateTime.now()));

        // 2. Đếm các mốc (giữ nguyên)
        summary.setMilestone1Count(progressRepository.countByUserSubjectIdAndCurrentBox(userSubjectId, 1));
        summary.setMilestone2Count(progressRepository.countByUserSubjectIdAndCurrentBox(userSubjectId, 2));
        summary.setMilestone3Count(progressRepository.countByUserSubjectIdAndCurrentBox(userSubjectId, 3));
        summary.setMilestone4Count(progressRepository.countByUserSubjectIdAndCurrentBox(userSubjectId, 4));
        summary.setMilestone5PlusCount(progressRepository.countByUserSubjectIdAndCurrentBoxGreaterThanEqual(userSubjectId, 5));

        // 3. === THÊM MỚI: TÌM NGÀY ÔN TẬP GẦN NHẤT ===
        Optional<LocalDateTime> nextDate = progressRepository
                .findNextReviewDateByUserSubjectId(userSubjectId, LocalDateTime.now());
        summary.setNextReviewTime(nextDate.orElse(null)); // Sẽ là null nếu không có thẻ nào

        return summary;
    }

    // (Hàm getFlashcardsForMilestoneForSubject giữ nguyên)
    @Transactional(readOnly = true)
    public FlashcardSetDTO getFlashcardsForMilestoneForSubject(Integer userSubjectId, Integer boxNumber) {
        List<Flashcard> cardsInMilestone;
        String title;
        if (boxNumber >= 5) {
            title = "Ôn tập: Mốc 5+ (Đã thuộc)";
            cardsInMilestone = progressRepository.findFlashcardsByUserSubjectIdAndCurrentBoxGreaterThanEqual(userSubjectId, 5);
        } else {
            title = "Ôn tập: Mốc " + boxNumber;
            cardsInMilestone = progressRepository.findFlashcardsByUserSubjectIdAndCurrentBox(userSubjectId, boxNumber);
        }
        List<FlashcardDTO> cardDTOs = cardsInMilestone.stream()
                .map(card -> modelMapper.map(card, FlashcardDTO.class))
                .collect(Collectors.toList());
        FlashcardSetDTO virtualSet = new FlashcardSetDTO();
        virtualSet.setTitle(title);
        virtualSet.setFlashcards(cardDTOs);
        virtualSet.setUserSubjectId(userSubjectId);
        return virtualSet;
    }
}