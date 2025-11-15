// Đặt tại: src/main/java/com/example/smartlearning/repository/TopicRepository.java
package com.example.smartlearning.repository;

import com.example.smartlearning.model.Subject;
import com.example.smartlearning.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Integer> {

    /**
     * Tìm một topic dựa trên tên và môn học
     */
    Optional<Topic> findByTopicNameAndSubject(String topicName, Subject subject);

    /**
     * (Chúng ta sẽ dùng cái này ở Bước 3)
     * Tìm một topic chỉ dựa vào tên (giả định tên là duy nhất)
     */
    Optional<Topic> findByTopicName(String topicName);
}