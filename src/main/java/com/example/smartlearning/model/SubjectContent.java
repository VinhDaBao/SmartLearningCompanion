// Đặt tại: src/main/java/com/example/smartlearning/model/SubjectContent.java
package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "SubjectContent")
public class SubjectContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Integer contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType; // "VIDEO", "TEXT", "PDF"

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "url_or_data", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String urlOrData;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
}