package com.example.smartlearning.controller;

import com.example.smartlearning.dto.SubjectDTO;
import com.example.smartlearning.model.Subject;
import com.example.smartlearning.service.SubjectService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * API lấy TẤT CẢ các môn học
     * URL: GET /api/subjects
     */
    @GetMapping
    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
        // 1. Lấy list Entity
        List<Subject> subjects = subjectService.getAllSubjects();

        // 2. Chuyển List<Entity> -> List<DTO>
        List<SubjectDTO> subjectDTOs = subjects.stream()
                .map(subject -> modelMapper.map(subject, SubjectDTO.class))
                .collect(Collectors.toList());

        // 3. Trả về list DTO
        return ResponseEntity.ok(subjectDTOs);
    }

    /**
     * API lấy một môn học cụ thể
     * URL: GET /api/subjects/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> getSubjectById(@PathVariable Integer id) {
        Subject subject = subjectService.getSubjectById(id);
        SubjectDTO subjectDTO = modelMapper.map(subject, SubjectDTO.class);
        return ResponseEntity.ok(subjectDTO);
    }
}