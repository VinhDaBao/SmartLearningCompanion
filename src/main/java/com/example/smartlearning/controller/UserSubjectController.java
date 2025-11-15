// Đặt tại: src/main/java/com/example/smartlearning/controller/UserSubjectController.java
package com.example.smartlearning.controller;

import com.example.smartlearning.dto.EnrollRequestDTO;
import com.example.smartlearning.dto.SubjectDTO;
import com.example.smartlearning.dto.UserSubjectDTO;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.service.UserSubjectService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Đảm bảo đã import đủ

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api") // Dùng chung /api
public class UserSubjectController {

    @Autowired
    private UserSubjectService userSubjectService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * API Lấy chi tiết MỘT môn học user đã đăng ký
     * (Bao gồm plans, quizzes, flashcards)
     * URL: GET /api/my-subject/{userSubjectId}
     *
     * ĐÂY CHÍNH LÀ API MÀ TRANG my-subject.html CẦN ĐỂ HOẠT ĐỘNG
     */
    @GetMapping("/my-subject/{userSubjectId}")
    public ResponseEntity<UserSubjectDTO> getEnrolledSubjectDetails(@PathVariable Integer userSubjectId) {

        // 1. Gọi service để lấy Entity (Giả định entity này đã liên kết
        //    với StudyPlans, Quizzes, FlashcardSets qua @OneToMany)
        UserSubject userSubject = userSubjectService.getEnrolledSubjectDetails(userSubjectId);

        // 2. Chuyển Entity sang DTO
        // (ModelMapper sẽ tự động map các list con nếu DTO của bạn
        // được định nghĩa để chứa các list đó)
        UserSubjectDTO dto = modelMapper.map(userSubject, UserSubjectDTO.class);

        // 3. Trả về cho frontend
        return ResponseEntity.ok(dto);
    }

    /**
     * API Lấy TẤT CẢ các môn user ĐÃ ĐĂNG KÝ
     * URL: GET /api/users/{userId}/subjects
     */
    @GetMapping("/users/{userId}/subjects")
    public ResponseEntity<List<UserSubjectDTO>> getEnrolledSubjects(@PathVariable Integer userId) {

        List<UserSubject> userSubjects = userSubjectService.getEnrolledSubjectsByUserId(userId);

        List<UserSubjectDTO> dtos = userSubjects.stream()
                .map(us -> modelMapper.map(us, UserSubjectDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * API để Đăng ký môn học mới
     * URL: POST /api/enroll
     */
    @PostMapping("/enroll")
    public ResponseEntity<UserSubjectDTO> enrollInSubject(@Valid @RequestBody EnrollRequestDTO enrollRequest) {

        UserSubject userSubject = userSubjectService.enrollUserInSubject(
                enrollRequest.getUserId(),
                enrollRequest.getSubjectId()
        );

        UserSubjectDTO dto = modelMapper.map(userSubject, UserSubjectDTO.class);
        return ResponseEntity.ok(dto);
    }
}