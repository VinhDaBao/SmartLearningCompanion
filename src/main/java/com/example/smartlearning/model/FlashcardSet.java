package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "FlashcardSet")
public class FlashcardSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "set_id")
    private Integer setId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "ai_model_used")
    private String aiModelUsed;

    // --- Mối quan hệ ---

    // Nhiều bộ flashcard có thể thuộc về MỘT lần đăng ký môn học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subject_id", nullable = false)
    private UserSubject userSubject;

    // Một bộ (Set) có NHIỀU thẻ (Flashcard)
    @OneToMany(
            mappedBy = "flashcardSet",
            cascade = CascadeType.ALL, // Xóa bộ thì xóa luôn các thẻ
            orphanRemoval = true
    )
    private List<Flashcard> flashcards = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}