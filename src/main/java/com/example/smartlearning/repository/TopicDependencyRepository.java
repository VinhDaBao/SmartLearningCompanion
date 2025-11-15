// Đặt tại: src/main/java/com/example/smartlearning/repository/TopicDependencyRepository.java
package com.example.smartlearning.repository;

import com.example.smartlearning.model.TopicDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicDependencyRepository extends JpaRepository<TopicDependency, Integer> {

    List<TopicDependency> findByTopic_Subject_SubjectId(Integer subjectId);
}