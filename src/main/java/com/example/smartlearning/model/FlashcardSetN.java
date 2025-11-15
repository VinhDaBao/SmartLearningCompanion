package com.example.smartlearning.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "FlashcardSet")
public class FlashcardSetN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer set_id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generated_at = LocalDateTime.now();

    @Column
    private String ai_model_used;

    // --- Quan hệ ManyToOne với User ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // --- Quan hệ OneToMany với Flashcard ---
    @OneToMany(mappedBy = "flashcardSetN", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashcardN> flashcards = new ArrayList<>();

    public void addFlashcard(FlashcardN card) {
        this.flashcards.add(card);
        card.setFlashcardSetN(this);
    }
}