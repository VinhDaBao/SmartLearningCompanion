//package com.example.smartlearning.controller;
//
//import com.example.smartlearning.dto.SubjectDTO;
//import com.example.smartlearning.model.Subject;
//import com.example.smartlearning.service.SubjectService;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/subjects")
//public class SubjectController {
//
//    @Autowired
//    private SubjectService subjectService;
//
//    @Autowired
//    private ModelMapper modelMapper;
//
//    /**
//     * API lấy TẤT CẢ các môn học
//     * URL: GET /api/subjects
//     */
//    @GetMapping
//    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
//        // 1. Lấy list Entity
//        List<Subject> subjects = subjectService.getAllSubjects();
//
//        // 2. Chuyển List<Entity> -> List<DTO>
//        List<SubjectDTO> subjectDTOs = subjects.stream()
//                .map(subject -> modelMapper.map(subject, SubjectDTO.class))
//                .collect(Collectors.toList());
//
//        // 3. Trả về list DTO
//        return ResponseEntity.ok(subjectDTOs);
//    }
//
//    /**
//     * API lấy một môn học cụ thể
//     * URL: GET /api/subjects/1
//     */
//    @GetMapping("/{id}")
//    public ResponseEntity<SubjectDTO> getSubjectById(@PathVariable Integer id) {
//        Subject subject = subjectService.getSubjectById(id);
//        SubjectDTO subjectDTO = modelMapper.map(subject, SubjectDTO.class);
//        return ResponseEntity.ok(subjectDTO);
//    }
//}
package com.example.smartlearning.controller;

import com.example.smartlearning.dto.SubjectDTO;
import com.example.smartlearning.model.Subject;
import com.example.smartlearning.repository.SubjectRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();
        List<SubjectDTO> subjectDTOs = subjects.stream()
                .map(subject -> modelMapper.map(subject, SubjectDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(subjectDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> getSubjectById(@PathVariable Integer id) {
        Optional<Subject> optionalSubject = subjectRepository.findById(id);
        if (optionalSubject.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SubjectDTO subjectDTO = modelMapper.map(optionalSubject.get(), SubjectDTO.class);
        return ResponseEntity.ok(subjectDTO);
    }

    @PostMapping
    public ResponseEntity<?> createSubject(@RequestBody SubjectDTO dto) {
        if (dto.getSubjectName() == null || dto.getSubjectName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên môn học không được để trống");
        }

        String normalizedName = dto.getSubjectName().trim();

        if (subjectRepository.existsBySubjectNameIgnoreCase(normalizedName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Môn học đã tồn tại trong hệ thống");
        }

        dto.setSubjectName(normalizedName);

        Subject subject = modelMapper.map(dto, Subject.class);
        subject.setSubjectId(null);

        Subject saved = subjectRepository.save(subject);
        SubjectDTO result = modelMapper.map(saved, SubjectDTO.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
