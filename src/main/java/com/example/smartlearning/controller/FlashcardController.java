// Đặt tại: src/main/java/com/example/smartlearning/controller/FlashcardController.java
package com.example.smartlearning.controller;

import com.example.smartlearning.dto.FlashcardRequestDTO;
import com.example.smartlearning.dto.FlashcardReviewDTO;
import com.example.smartlearning.dto.FlashcardReviewSummaryDTO;
import com.example.smartlearning.dto.FlashcardSetDTO;
import com.example.smartlearning.model.FlashcardSet;
import com.example.smartlearning.model.User;
import com.example.smartlearning.repository.UserRepository;
import com.example.smartlearning.service.FileContentService;
import com.example.smartlearning.service.FlashcardService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;


@RestController
@RequestMapping("/api") // <-- SỬA: Dùng API chung
public class FlashcardController {

    @Autowired
    private FlashcardService flashcardService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FileContentService fileContentService;
    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User (lỗi bảo mật)"));
    }


    @PostMapping(
            value = "/flashcards/generate", // (API này giữ nguyên global)
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FlashcardSetDTO> generateFlashcardSet(
            @Valid @RequestPart("request") FlashcardRequestDTO requestDTO,
            @RequestPart(value = "lectureFile", required = false) MultipartFile lectureFile
    ) {
        String lectureText = fileContentService.extractText(lectureFile);
        FlashcardSet newSet = flashcardService.createFlashcardSet(requestDTO, lectureText);
        FlashcardSetDTO responseDTO = modelMapper.map(newSet, FlashcardSetDTO.class);
        return ResponseEntity.ok(responseDTO);
    }


    @GetMapping("/flashcards/{setId}") // (API này giữ nguyên global)
    public ResponseEntity<FlashcardSetDTO> getFlashcardSetById(@PathVariable Integer setId) {
        FlashcardSetDTO setDetails = flashcardService.getFlashcardSetDetails(setId);
        return ResponseEntity.ok(setDetails);
    }


    @PostMapping("/flashcards/review") // (API này giữ nguyên global)
    public ResponseEntity<?> reviewFlashcard(@Valid @RequestBody FlashcardReviewDTO reviewDTO) {
        User currentUser = getCurrentUser();
        flashcardService.reviewFlashcard(currentUser, reviewDTO);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/flashcards/review/due") // (API này giữ nguyên global - cho thông báo 🔔)
    public ResponseEntity<FlashcardSetDTO> getDueReviewFlashcardsApi() {
        User currentUser = getCurrentUser();
        FlashcardSetDTO dto = flashcardService.getDueReviewFlashcards(currentUser.getUserId());
        return ResponseEntity.ok(dto);
    }

    // =================================================================
    // === API MỚI 1 (SỬA LẠI): LẤY TÓM TẮT CÁC MỐC (THEO MÔN HỌC) ===
    // =================================================================
    @GetMapping("/my-subject/{userSubjectId}/review-summary")
    public ResponseEntity<FlashcardReviewSummaryDTO> getReviewSummaryForSubject(
            @PathVariable Integer userSubjectId
    ) {
        // (Chúng ta không cần check user vì userSubjectId đã là duy nhất)
        FlashcardReviewSummaryDTO summary = flashcardService.getReviewSummaryForSubject(userSubjectId);
        return ResponseEntity.ok(summary);
    }

    // =================================================================
    // === API MỚI 2 (SỬA LẠI): LẤY THẺ THEO MỐC (THEO MÔN HỌC) ===
    // =================================================================
    @GetMapping("/my-subject/{userSubjectId}/review/milestone/{boxNumber}")
    public ResponseEntity<FlashcardSetDTO> getFlashcardsForMilestoneApi(
            @PathVariable Integer userSubjectId,
            @PathVariable Integer boxNumber
    ) {
        FlashcardSetDTO dto = flashcardService.getFlashcardsForMilestoneForSubject(userSubjectId, boxNumber);
        return ResponseEntity.ok(dto);
    }
}