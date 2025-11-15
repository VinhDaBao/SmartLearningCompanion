package com.example.smartlearning.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "FlashcardN")
public class FlashcardN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer flashcard_id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String front_text;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String back_text;

    // --- Quan hệ ManyToOne với FlashcardSet ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private FlashcardSetN flashcardSetN;
}
