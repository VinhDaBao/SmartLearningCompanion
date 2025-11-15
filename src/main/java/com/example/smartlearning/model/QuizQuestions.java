package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "QuizQuestions")
public class QuizQuestions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Integer questionId;

    @Lob
    @Column(name = "question_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String questionText;

    // Lưu các lựa chọn dưới dạng JSON
    @Lob
    @Column(name = "options", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String options; // Ví dụ: {"A": "Lựa chọn 1", "B": "Lựa chọn 2"}

    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer; // Ví dụ: "A"

    @Lob
    @Column(name = "explanation", columnDefinition = "NVARCHAR(MAX)")
    private String explanation; // Giải thích của AI

    // --- Mối quan hệ ---
    // Nhiều câu hỏi thuộc về MỘT bộ Quiz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
}