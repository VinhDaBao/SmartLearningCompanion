// Đặt tại: src/main/java/com/example/smartlearning/dto/UserSubjectDTO.java
package com.example.smartlearning.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // <-- IMPORT MỚI

@Data
public class UserSubjectDTO {
    private Integer id; // Đây chính là userSubjectId
    private Integer progressPercentage;
    private Double currentScore;
    private LocalDateTime enrolledAt;

    // Chúng ta lồng DTO để biết đây là môn nào
    private SubjectDTO subject;

    // === PHẦN SỬA LỖI 3 ===
    // Bổ sung các list mà my-subject.html cần
    // (Hãy đảm bảo bạn đã import các DTO này)
    private List<StudyPlanInfoDTO> studyPlans;
    private List<QuizInfoDTO> quizzes;
    private List<FlashcardSetInfoDTO> flashcardSets;
}