package com.example.smartlearning.repository;

import com.example.smartlearning.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    boolean existsBySubjectNameIgnoreCase(String subjectName);
}
