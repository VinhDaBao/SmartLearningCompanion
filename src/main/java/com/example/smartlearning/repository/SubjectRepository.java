package com.example.smartlearning.repository;

import com.example.smartlearning.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    // Hiện tại chúng ta chưa cần phương thức tùy chỉnh nào
    // JpaRepository đã cung cấp đủ (findById, findAll, save)
}