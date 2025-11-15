package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Flashcard")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flashcard_id")
    private Integer flashcardId;

    @Lob
    @Column(name = "front_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String frontText; // Mặt trước

    @Lob
    @Column(name = "back_text", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String backText; // Mặt sau

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private FlashcardSet flashcardSet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;
}