// Đặt tại: src/main/java/com/example/smartlearning/repository/UserFlashcardProgressRepository.java
package com.example.smartlearning.repository;

import com.example.smartlearning.model.Flashcard;
import com.example.smartlearning.model.UserFlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserFlashcardProgressRepository extends JpaRepository<UserFlashcardProgress, Integer> {

    // (Giữ nguyên, đã sửa đúng)
    Optional<UserFlashcardProgress> findByUser_UserIdAndFlashcard_FlashcardId(Integer userId, Integer flashcardId);

    // (Giữ nguyên - Đếm TẤT CẢ thẻ tới hạn)
    @Query("SELECT COUNT(p) FROM UserFlashcardProgress p WHERE p.user.userId = :userId AND p.nextReviewDate <= :now")
    int countDueReviews(@Param("userId") Integer userId, @Param("now") LocalDateTime now);

    // (Giữ nguyên - Lấy TẤT CẢ thẻ tới hạn)
    @Query("SELECT p.flashcard FROM UserFlashcardProgress p WHERE p.user.userId = :userId AND p.nextReviewDate <= :now ORDER BY p.nextReviewDate ASC")
    List<Flashcard> findDueFlashcards(@Param("userId") Integer userId, @Param("now") LocalDateTime now);

    // =================================================================
    // === CÁC HÀM CHO TÍNH NĂNG CHỦ ĐỘNG (THEO MÔN HỌC) ===
    // =================================================================

    // (Đếm thẻ tới hạn CỦA MỘT MÔN)
    @Query("SELECT COUNT(p) FROM UserFlashcardProgress p " +
            "WHERE p.flashcard.flashcardSet.userSubject.id = :userSubjectId AND p.nextReviewDate <= :now")
    int countDueReviewsForSubject(@Param("userSubjectId") Integer userSubjectId, @Param("now") LocalDateTime now);

    // (Đếm thẻ trong Mốc CỦA MỘT MÔN)
    @Query("SELECT COUNT(p) FROM UserFlashcardProgress p " +
            "WHERE p.flashcard.flashcardSet.userSubject.id = :userSubjectId AND p.currentBox = :box")
    long countByUserSubjectIdAndCurrentBox(@Param("userSubjectId") Integer userSubjectId, @Param("box") int box);

    // (Đếm thẻ Master CỦA MỘT MÔN)
    @Query("SELECT COUNT(p) FROM UserFlashcardProgress p " +
            "WHERE p.flashcard.flashcardSet.userSubject.id = :userSubjectId AND p.currentBox >= 5")
    long countByUserSubjectIdAndCurrentBoxGreaterThanEqual(@Param("userSubjectId") Integer userSubjectId, @Param("box") int box);

    // (Lấy thẻ trong Mốc CỦA MỘT MÔN)
    @Query("SELECT p.flashcard FROM UserFlashcardProgress p " +
            "WHERE p.flashcard.flashcardSet.userSubject.id = :userSubjectId AND p.currentBox = :box")
    List<Flashcard> findFlashcardsByUserSubjectIdAndCurrentBox(@Param("userSubjectId") Integer userSubjectId, @Param("box") int box);

    // (Lấy thẻ Master CỦA MỘT MÔN)
    @Query("SELECT p.flashcard FROM UserFlashcardProgress p " +
            "WHERE p.flashcard.flashcardSet.userSubject.id = :userSubjectId AND p.currentBox >= 5")
    List<Flashcard> findFlashcardsByUserSubjectIdAndCurrentBoxGreaterThanEqual(@Param("userSubjectId") Integer userSubjectId, @Param("box") int box);

    // =================================================================
    // === BẮT ĐẦU THÊM MỚI (CHO ĐỒNG HỒ ĐẾM NGƯỢC) ===
    // =================================================================

    /**
     * Tìm ngày ôn tập (trong tương lai) gần nhất của một môn học
     */
    @Query("SELECT MIN(p.nextReviewDate) FROM UserFlashcardProgress p " +
            "WHERE p.flashcard.flashcardSet.userSubject.id = :userSubjectId AND p.nextReviewDate > :now")
    Optional<LocalDateTime> findNextReviewDateByUserSubjectId(@Param("userSubjectId") Integer userSubjectId, @Param("now") LocalDateTime now);
}