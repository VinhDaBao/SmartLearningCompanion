// Đặt tại: src/main/java/com/example/smartlearning/model/TopicDependency.java
package com.example.smartlearning.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "TopicDependency")
public class TopicDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dependency_id")
    private Integer dependencyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic; // Con

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_topic_id")
    private Topic dependsOnTopic; // Cha
}