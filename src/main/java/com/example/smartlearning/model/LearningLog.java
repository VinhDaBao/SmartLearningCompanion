package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "LearningLog")
public class LearningLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @Column(name = "activity_type", nullable = false)
    private String activityType; // Ví dụ: "QUIZ_TAKEN", "PLAN_GENERATED"

    @Column(name = "details")
    private String details; // Ví dụ: "Scored 8/10 on Quiz 5"

    @Column(name = "duration_minutes")
    private Integer durationMinutes; // Thời gian học (nếu có)

    @Column(name = "log_time", updatable = false)
    private LocalDateTime logTime;

    // --- Mối quan hệ ---

    // Nhiều log thuộc về MỘT User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Nhiều log có thể thuộc về MỘT môn học (nhưng có thể NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subject_id", nullable = true) // Cho phép NULL
    private UserSubject userSubject;

    @PrePersist
    protected void onCreate() {
        if (logTime == null) {
            logTime = LocalDateTime.now();
        }
    }
}