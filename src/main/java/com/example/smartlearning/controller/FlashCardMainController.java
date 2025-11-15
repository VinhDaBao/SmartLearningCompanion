package com.example.smartlearning.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.smartlearning.dto.FlashcardDTO;
import com.example.smartlearning.model.FlashcardSet;
import com.example.smartlearning.service.AiGenerationService;
import com.example.smartlearning.service.FlashcardService;
import com.example.smartlearning.service.PDFService;
import com.example.smartlearning.service.UserSubjectService;

@RestController
@RequestMapping("/api/flashcards")
public class FlashCardMainController {
	
	@Autowired
	PDFService pdfService;
	
	@Autowired
	UserSubjectService userSubjectService;
	 @PostMapping("/sets")
	    public String showFlashcardSets() {
		    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	        return auth.getName();  
	    }

}
