package com.example.smartlearning.repository;

import com.example.smartlearning.model.UserSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubjectRepository extends JpaRepository<UserSubject, Integer> {

    // --- (PHẦN ĐÃ SỬA) ---
    // Spring Data JPA sẽ dịch "UserUserId" thành "user.userId"
    // Spring Data JPA sẽ dịch "SubjectSubjectId" thành "subject.subjectId"

    /**
     * Lấy một bản ghi UserSubject cụ thể bằng user_id và subject_id
     * Code cũ (lỗi): findByUserIdAndSubjectId
     */
    Optional<UserSubject> findByUserUserIdAndSubjectSubjectId(Integer userId, Integer subjectId);

    /**
     * Lấy TẤT CẢ các môn học mà một User đã đăng ký
     * Code cũ (lỗi): findByUserId
     */
    List<UserSubject> findByUserUserId(Integer userId);

    /**
     * Lấy TẤT CẢ các sinh viên đã đăng ký một môn học
     * Code cũ (lỗi): findBySubjectId
     */
    List<UserSubject> findBySubjectSubjectId(Integer subjectId);
}