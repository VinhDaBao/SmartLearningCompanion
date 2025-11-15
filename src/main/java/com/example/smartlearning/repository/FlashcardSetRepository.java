package com.example.smartlearning.repository;

import com.example.smartlearning.model.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Integer> {
    // Lấy tất cả các bộ flashcard cho một môn học của user
    List<FlashcardSet> findByUserSubjectId(Integer userSubjectId);
}