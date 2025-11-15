// Đặt tại: src/main/java/com/example/smartlearning/repository/SubjectContentRepository.java
package com.example.smartlearning.repository;

import com.example.smartlearning.model.SubjectContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectContentRepository extends JpaRepository<SubjectContent, Integer> {


    List<SubjectContent> findByTopic_TopicIdAndContentType(Integer topicId, String contentType);
    List<SubjectContent> findByTopic_TopicId(Integer topicId);
}