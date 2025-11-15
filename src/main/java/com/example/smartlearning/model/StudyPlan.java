package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "StudyPlan")
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_plan_id")
    private Integer studyPlanId;

    @Lob
    @Column(name = "plan_content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String planContent;

    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "ai_model_used")
    private String aiModelUsed;

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