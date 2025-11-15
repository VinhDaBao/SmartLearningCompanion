package com.example.smartlearning.controller;
import com.example.smartlearning.dto.FlashcardDTO;
import com.example.smartlearning.dto.FlashcardRequestDTO;
import com.example.smartlearning.dto.FlashcardSetDTO;
import com.example.smartlearning.model.FlashcardSet;
import com.example.smartlearning.service.AiGenerationService;
import com.example.smartlearning.service.FlashcardService;
import jakarta.validation.Valid;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * API để Frontend kích hoạt việc sinh Bộ Flashcard mới
     * URL: POST /api/flashcards/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<FlashcardSetDTO> generateFlashcardSet(
            @Valid @RequestBody FlashcardRequestDTO requestDTO) {

        // 1. Gọi Service
        FlashcardSet newSet = flashcardService.createFlashcardSet(requestDTO);

        // 2. Map Entity -> DTO
        // Lần này ModelMapper có thể tự map (vì tên trường giống nhau)
        FlashcardSetDTO responseDTO = modelMapper.map(newSet, FlashcardSetDTO.class);

        // 3. Trả DTO về cho Frontend
        return ResponseEntity.ok(responseDTO);
    }
    @GetMapping("/{setId}")
    public ResponseEntity<FlashcardSetDTO> getFlashcardSetById(@PathVariable Integer setId) {

        FlashcardSetDTO setDetails = flashcardService.getFlashcardSetDetails(setId);

        return ResponseEntity.ok(setDetails);
    }
    @Autowired
	AiGenerationService ai;
	@PostMapping("/pdf")
	public ResponseEntity<?> createFromPdf(
	        @RequestParam MultipartFile file,
	        @RequestParam(defaultValue = "50") int numCards
	) {
	    List<FlashcardDTO> list =
	            ai.generateFlashcardsFromLargePdf(file, numCards);

	    return ResponseEntity.ok(list);
	}
}