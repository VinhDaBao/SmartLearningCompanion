package com.example.smartlearning.service;

import com.example.smartlearning.model.Subject;
import com.example.smartlearning.model.User;
import com.example.smartlearning.model.UserSubject;
import com.example.smartlearning.repository.SubjectRepository;
import com.example.smartlearning.repository.UserRepository;
import com.example.smartlearning.repository.UserSubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserSubjectService {

    @Autowired
    private UserSubjectRepository userSubjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    /**
     * Lấy tất cả các môn học mà một user đã đăng ký.
     */
    public List<UserSubject> getEnrolledSubjectsByUserId(Integer userId) {

        // --- (ĐÂY LÀ DÒNG ĐÃ SỬA) ---
        // Code cũ (lỗi): findByUserId(userId)
        return userSubjectRepository.findByUserUserId(userId);
    }

    /**
     * Lấy chi tiết một môn học đã đăng ký.
     * (Hàm này dùng findById nên không lỗi, giữ nguyên)
     */
    public UserSubject getEnrolledSubjectDetails(Integer userSubjectId) {
        return userSubjectRepository.findById(userSubjectId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found: " + userSubjectId));
    }

    /**
     * Logic nghiệp vụ: Đăng ký cho sinh viên học một môn mới.
     */
    @Transactional
    public UserSubject enrollUserInSubject(Integer userId, Integer subjectId) {

        // 1. Kiểm tra xem đã đăng ký chưa
        // --- (ĐÂY LÀ DÒNG ĐÃ SỬA) ---
        // Code cũ (lỗi): findByUserIdAndSubjectId(userId, subjectId)
        Optional<UserSubject> existing = userSubjectRepository.findByUserUserIdAndSubjectSubjectId(userId, subjectId);

        if (existing.isPresent()) {
            throw new RuntimeException("User already enrolled in this subject");
        }

        // 2. Lấy (hoặc xác thực) User và Subject
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // 3. Tạo bản ghi đăng ký mới
        UserSubject newUserSubject = new UserSubject();
        newUserSubject.setUser(user);
        newUserSubject.setSubject(subject);
        newUserSubject.setProgressPercentage(0);

        // 4. Lưu vào database
        return userSubjectRepository.save(newUserSubject);
    }
}