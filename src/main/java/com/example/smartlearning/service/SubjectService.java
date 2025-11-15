package com.example.smartlearning.service;

import com.example.smartlearning.model.Subject;
import com.example.smartlearning.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    /**
     * Lấy danh sách TẤT CẢ các môn học.
     * @return List<Subject>
     */
    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    /**
     * Lấy thông tin một môn học bằng ID.
     * @param subjectId ID môn học
     * @return Đối tượng Subject
     * @throws RuntimeException nếu không tìm thấy
     */
    public Subject getSubjectById(Integer subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + subjectId));
    }
}