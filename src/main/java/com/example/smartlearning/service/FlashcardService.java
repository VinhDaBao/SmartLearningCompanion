package com.example.smartlearning.service;

import com.example.smartlearning.dto.FlashcardRequestDTO;
import com.example.smartlearning.dto.ai.AiFlashcardDTO;
import com.example.smartlearning.model.Flashcard;
import com.example.smartlearning.model.FlashcardSet;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.FlashcardRepository;
import com.example.smartlearning.repository.FlashcardSetRepository;
import com.example.smartlearning.repository.UserSubjectRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// MỚI: Import service log
import com.example.smartlearning.service.LearningLogService;

@Service
public class FlashcardService {

    @Autowired
    private FlashcardSetRepository flashcardSetRepository;

    @Autowired
    private FlashcardRepository flashcardRepository;
    
    @Autowired
    private UserSubjectRepository userSubjectRepository;

    @Autowired
    private AiGenerationService aiGenerationService;

    @Autowired
    private ObjectMapper objectMapper;

    // MỚI: Tiêm (inject) LearningLogService
    @Autowired
    private LearningLogService learningLogService;

    @Transactional
    public FlashcardSet createFlashcardSet(FlashcardRequestDTO requestDTO) {

        // 1. Lấy thông tin User và Subject
        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));

        // 2. Gọi Service AI để sinh nội dung (một chuỗi JSON)
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
            newSet.setAiModelUsed("gpt-4o-mini-mock-json");
            
            FlashcardSet savedSet = flashcardSetRepository.save(newSet);

            List<Flashcard> cardEntities = aiCards.stream().map(aiCard -> {
                Flashcard card = new Flashcard();
                card.setFlashcardSet(newSet);
                card.setFrontText(aiCard.getFront());
                card.setBackText(aiCard.getBack());
                return card;
            }).collect(Collectors.toList());

            savedSet.setFlashcards(cardEntities);

            // 5. Lưu vào DB
            // MỚI: Gán vào biến 'savedSet'
            flashcardSetRepository.save(savedSet);

            // 6. GHI LOG (MỚI)
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

            return savedSet; // Trả về entity đã được lưu

        } catch (Exception e) {
            System.err.println("Không thể parse JSON từ AI (Flashcard): " + e.getMessage());
            System.err.println("JSON nhận được: " + jsonResponse);
            throw new RuntimeException("Lỗi xử lý dữ liệu từ AI", e);
        }
    }
    public FlashcardSet createFlashcardSetMock(FlashcardRequestDTO requestDTO) {
        UserSubject us = userSubjectRepository.findById(requestDTO.getUserSubjectId())
            .orElseThrow(() -> new IllegalArgumentException("UserSubject not found"));

        FlashcardSet newSet = new FlashcardSet();
        newSet.setUserSubject(us);
        newSet.setTitle("Bộ flashcard test");
        newSet.setAiModelUsed("mock-AI");

        // Lưu parent để có id (managed)
        FlashcardSet savedSet = flashcardSetRepository.saveAndFlush(newSet);
        System.out.println("savedSet.id = " + savedSet.getSetId());

        // Dữ liệu để tạo card
        List<String[]> cardData = List.of(
            new String[] {"Spring Boot", "Framework phát triển nhanh các ứng dụng Spring"},
            new String[] {"Hibernate", "ORM cho Java"},
            new String[] {"JPA", "Java Persistence API"}
        );

        // Tạo và thêm từng card vào collection quản lý của savedSet
        for (String[] d : cardData) {
            Flashcard card = createMockCard(d[0], d[1]); // KHÔNG save ở đây
            savedSet.addFlashcard(card); // helper: adds to list AND card.setFlashcardSet(this)
        }

        System.out.println("1 cards added, size = " + savedSet.getFlashcards().size());
        System.out.println("2 about to save savedSet (will cascade to cards)");

        // Lưu parent — cascade = ALL sẽ persist children
        flashcardSetRepository.saveAndFlush(savedSet);

        System.out.println("3 savedSet persisted with cards, id=" + savedSet.getSetId());
        return savedSet;
    }

    private Flashcard createMockCard(String front, String back) {
        Flashcard card = new Flashcard();
        card.setFrontText(front);
        card.setBackText(back);
        return card;
    }
}