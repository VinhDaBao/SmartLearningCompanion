package com.example.smartlearning.service;

import com.example.smartlearning.dto.FlashcardRequestDTO;
import com.example.smartlearning.dto.FlashcardSetDTO;
import com.example.smartlearning.dto.ai.AiFlashcardDTO;
import com.example.smartlearning.model.Flashcard;
import com.example.smartlearning.model.FlashcardSet;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.FlashcardSetRepository;
import com.example.smartlearning.repository.UserSubjectRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.example.smartlearning.service.LearningLogService;

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

    @Autowired
    private LearningLogService learningLogService;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public FlashcardSet createFlashcardSet(FlashcardRequestDTO requestDTO) {

        // --- (ĐÂY LÀ DÒNG SỬA LỖI) ---
        // Chúng ta tìm bằng 'userSubjectId' (là ID của bản ghi đăng ký)
        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));
        // --- (HẾT PHẦN SỬA LỖI) ---

        // 2. Gọi Service AI
        String jsonResponse = aiGenerationService.generateFlashcards(
                userSubject.getUser(),
                userSubject.getSubject(),
                requestDTO.getTopic(),
                requestDTO.getNumberOfCards()
        );

        // 3. Phân tích (Parse) chuỗi JSON
        try {
            List<AiFlashcardDTO> aiCards = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<List<AiFlashcardDTO>>() {}
            );

            // 4. Tạo các Entity
            FlashcardSet newSet = new FlashcardSet();
            newSet.setUserSubject(userSubject);
            newSet.setTitle(requestDTO.getTitle());
            newSet.setAiModelUsed("gemini-pro"); // (Hoặc model AI bạn đang dùng)

            List<Flashcard> cardEntities = aiCards.stream().map(aiCard -> {
                Flashcard card = new Flashcard();
                card.setFlashcardSet(newSet);
                card.setFrontText(aiCard.getFront());
                card.setBackText(aiCard.getBack());
                return card;
            }).collect(Collectors.toList());

            newSet.setFlashcards(cardEntities);
            FlashcardSet savedSet = flashcardSetRepository.save(newSet);

            // 6. GHI LOG
            try {
                learningLogService.logActivity(
                        userSubject.getUser(),
                        userSubject,
                        "FLASHCARD_GENERATED",
                        "Đã tạo bộ flashcard: " + savedSet.getTitle(),
                        0
                );
            } catch (Exception e) {
                System.err.println("Lỗi ghi log (Flashcard): " + e.getMessage());
            }

            return savedSet;

        } catch (Exception e) {
            System.err.println("Không thể parse JSON từ AI (Flashcard): " + e.getMessage());
            System.err.println("JSON nhận được: " + jsonResponse);
            throw new RuntimeException("Lỗi xử lý dữ liệu từ AI", e);
        }
    }

    /**
     * Lấy chi tiết đầy đủ của một bộ Flashcard (bao gồm tất cả các thẻ)
     * (Hàm này đã đúng, giữ nguyên)
     */
    @Transactional(readOnly = true)
    public FlashcardSetDTO getFlashcardSetDetails(Integer setId) {

        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ Flashcard: " + setId));

        FlashcardSetDTO setDTO = modelMapper.map(flashcardSet, FlashcardSetDTO.class);

        return setDTO;
    }
}