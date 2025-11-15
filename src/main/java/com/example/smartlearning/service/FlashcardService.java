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

        UserSubject userSubject = userSubjectRepository.findById(requestDTO.getUserSubjectId())
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));

        String jsonResponse = aiGenerationService.generateFlashcards(
                userSubject.getUser(),
                userSubject.getSubject(),
                requestDTO.getTopic(),
                requestDTO.getNumberOfCards()
        );

        try {
            List<AiFlashcardDTO> aiCards = objectMapper.readValue(
                    jsonResponse,
                    new TypeReference<List<AiFlashcardDTO>>() {}
            );

            FlashcardSet newSet = new FlashcardSet();
            newSet.setUserSubject(userSubject);
            newSet.setTitle(requestDTO.getTitle());
            newSet.setAiModelUsed("gemini-pro");

            List<Flashcard> cardEntities = aiCards.stream().map(aiCard -> {
                Flashcard card = new Flashcard();
                card.setFlashcardSet(newSet);
                card.setFrontText(aiCard.getFrontText());
                card.setBackText(aiCard.getBackText());
                return card;
            }).collect(Collectors.toList());

            newSet.setFlashcards(cardEntities);
            FlashcardSet savedSet = flashcardSetRepository.save(newSet);

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

    @Transactional(readOnly = true)
    public FlashcardSetDTO getFlashcardSetDetails(Integer setId) {

        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ Flashcard: " + setId));

        FlashcardSetDTO setDTO = modelMapper.map(flashcardSet, FlashcardSetDTO.class);
        setDTO.setUserSubjectId(flashcardSet.getUserSubject().getId());

        return setDTO;
    }
}