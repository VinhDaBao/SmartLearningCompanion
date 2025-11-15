package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Quiz")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Integer quizId;

    @Column(name = "title")
    private String title;

    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "ai_model_used")
    private String aiModelUsed;

    // --- Mối quan hệ ---

    // Nhiều bộ quiz có thể thuộc về MỘT lần đăng ký môn học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subject_id", nullable = false)
    private UserSubject userSubject;

    // Một bộ Quiz có NHIỀU câu hỏi
    @OneToMany(
            mappedBy = "quiz",
            cascade = CascadeType.ALL, // Xóa Quiz thì xóa luôn các câu hỏi
            orphanRemoval = true
    )
    private List<QuizQuestions> questions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}