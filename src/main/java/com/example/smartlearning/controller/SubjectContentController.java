// Đặt tại: src/main/java/com/example/smartlearning/controller/SubjectContentController.java
package com.example.smartlearning.controller;

import com.example.smartlearning.dto.SubjectContentDTO;
import com.example.smartlearning.service.SubjectContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SubjectContentController {

    @Autowired
    private SubjectContentService subjectContentService;

    @GetMapping("/my-subject/{userSubjectId}/content")
    public ResponseEntity<SubjectContentDTO> getSubjectContent(
            @PathVariable Integer userSubjectId) {

        SubjectContentDTO content = subjectContentService.getSubjectContent(userSubjectId);
        return ResponseEntity.ok(content);
    }
}