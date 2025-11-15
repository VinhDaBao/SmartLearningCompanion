package com.example.smartlearning.dto;

import lombok.Data;
import java.util.List;

@Data
public class SubjectContentDTO {
    // Thông tin về môn học
    private Integer userSubjectId;
    private String subjectName;
    private String subjectDescription;
    private int progressPercentage;

    // Danh sách nội dung AI đã tạo
    private List<StudyPlanDTO> studyPlans;
    private List<QuizInfoDTO> quizzes;
    private List<FlashcardSetInfoDTO> flashcardSets;
}