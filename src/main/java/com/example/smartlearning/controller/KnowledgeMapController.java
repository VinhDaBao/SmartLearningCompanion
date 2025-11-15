// Đặt tại: src/main/java/com/example/smartlearning/controller/KnowledgeMapController.java
package com.example.smartlearning.controller;

import com.example.smartlearning.dto.KnowledgeMapDTO;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.UserSubjectRepository;
import com.example.smartlearning.service.KnowledgeMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; // <-- Rất quan trọng
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/knowledge-map")
public class KnowledgeMapController {

    @Autowired
    private KnowledgeMapService mapService;

    @Autowired
    private UserSubjectRepository userSubjectRepo;

    @GetMapping("/{userSubjectId}")
    @Transactional(readOnly = true) // <-- Thêm cái này để tránh lỗi Lazy
    public ResponseEntity<KnowledgeMapDTO> getMap(@PathVariable Integer userSubjectId) {

        UserSubject userSubject = userSubjectRepo.findById(userSubjectId)
                .orElseThrow(() -> new RuntimeException("UserSubject not found"));

        KnowledgeMapDTO mapDTO = mapService.getKnowledgeMap(userSubjectId, userSubject.getSubject());

        return ResponseEntity.ok(mapDTO);
    }
}