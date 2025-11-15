package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "study_plan")
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_plan_id")
    private Integer studyPlanId;

    // @Lob dùng cho các kiểu dữ liệu lớn như NVARCHAR(MAX)
    @Lob
    @Column(name = "plan_content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String planContent; // Nội dung lộ trình (text, JSON, hoặc Markdown)

    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "ai_model_used")
    private String aiModelUsed; // Ví dụ: "gpt-4o-mini"

    // --- Mối quan hệ ---
    // Nhiều lộ trình có thể thuộc về MỘT lần đăng ký môn học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subject_id", nullable = false)
    private UserSubject userSubject;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}