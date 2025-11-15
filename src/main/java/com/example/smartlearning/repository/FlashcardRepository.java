package com.example.smartlearning.repository;

import com.example.smartlearning.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {
    long countByTopic_TopicId(Integer topicId);
}