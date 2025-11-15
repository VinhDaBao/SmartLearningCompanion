// Đặt tại: src/main/java/com/example/smartlearning/model/Topic.java
package com.example.smartlearning.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Topic", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"subject_id", "topic_name"})
})
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Integer topicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "topic_name", nullable = false, length = 150)
    private String topicName;

    // Bỏ qua constructor, getter/setter cho ngắn gọn
    // (Bạn tự tạo hoặc dùng Lombok)

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}