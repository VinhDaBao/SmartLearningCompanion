// Đặt tại: src/main/java/com/example/smartlearning/model/UserFlashcardProgress.java
package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor // Cần constructor rỗng cho JPA
@Entity
@Table(name = "UserFlashcardProgress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "flashcard_id"})
)
public class UserFlashcardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Integer progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(name = "current_box", nullable = false)
    private int currentBox = 1;

    @Column(name = "next_review_date", nullable = false)
    private LocalDateTime nextReviewDate;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    // Constructor tiện lợi khi tạo mới
    public UserFlashcardProgress(User user, Flashcard flashcard) {
        this.user = user;
        this.flashcard = flashcard;
        this.nextReviewDate = LocalDateTime.now(); // Học ngay
    }
}