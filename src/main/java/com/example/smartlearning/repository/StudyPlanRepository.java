package com.example.smartlearning.repository;

import com.example.smartlearning.model.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Integer> {
    // Lấy tất cả các lộ trình cho một môn học của user
    List<StudyPlan> findByUserSubjectId(Integer userSubjectId);
}