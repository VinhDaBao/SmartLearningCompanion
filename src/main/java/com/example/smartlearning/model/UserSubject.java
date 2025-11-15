package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "UserSubjects",
        // Đảm bảo cặp (user_id, subject_id) là duy nhất
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "subject_id"})
)
public class UserSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_subject_id")
    private Integer id; // Khóa chính của riêng bảng này

    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage = 0; // Gán giá trị mặc định

    @Column(name = "current_score")
    private Double currentScore;

    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;

    // --- Mối quan hệ MANY-TO-ONE ---

    // Nhiều 'UserSubject' thuộc về một 'User'
    @ManyToOne(fetch = FetchType.LAZY) // LAZY = Chỉ tải khi cần
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Nhiều 'UserSubject' thuộc về một 'Subject'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // --- Mối quan hệ ONE-TO-MANY ---

    // Một UserSubject có thể có nhiều lộ trình học
    @OneToMany(mappedBy = "userSubject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyPlan> studyPlans = new ArrayList<>();

    // Một UserSubject có thể có nhiều bộ Quiz
    @OneToMany(mappedBy = "userSubject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();

    // Một UserSubject có thể có nhiều bộ Flashcard
    @OneToMany(mappedBy = "userSubject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardSet> flashcardSets = new ArrayList<>();

    // Chúng ta sẽ thêm LearningLog sau

    @PrePersist
    protected void onCreate() {
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }
    }
}