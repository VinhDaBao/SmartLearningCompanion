// Đặt tại: src/main/java/com/example/smartlearning/service/KnowledgeMapService.java
package com.example.smartlearning.service;

import com.example.smartlearning.dto.GraphEdgeDTO;
import com.example.smartlearning.dto.GraphNodeDTO;
import com.example.smartlearning.dto.KnowledgeMapDTO;
import com.example.smartlearning.dto.TopicMasteryDTO;
import com.example.smartlearning.model.Subject;
import com.example.smartlearning.model.Topic;
import com.example.smartlearning.repository.QuizQuestionsRepository; // <-- THÊM MỚI
import com.example.smartlearning.repository.FlashcardRepository; // <-- THÊM MỚI
import com.example.smartlearning.repository.TopicDependencyRepository;
import com.example.smartlearning.repository.TopicRepository;
import com.example.smartlearning.repository.UserQuestionAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KnowledgeMapService {

    @Autowired
    private TopicRepository topicRepo;

    @Autowired
    private TopicDependencyRepository dependencyRepo;

    @Autowired
    private UserQuestionAnswerRepository answerRepo;

    // --- BẮT ĐẦU THÊM REPO ---
    @Autowired
    private QuizQuestionsRepository quizQuestionsRepo;

    @Autowired
    private FlashcardRepository flashcardRepo;
    // --- KẾT THÚC THÊM REPO ---

    @Transactional(readOnly = true)
    public KnowledgeMapDTO getKnowledgeMap(Integer userSubjectId, Subject subject) {

        // 1. Lấy tất cả điểm thông thạo
        Map<Integer, Double> masteryMap = answerRepo.getMasteryScoresForUserSubject(userSubjectId)
                .stream()
                .collect(Collectors.toMap(TopicMasteryDTO::getTopicId, TopicMasteryDTO::getMasteryScore));

        // 2. Lấy tất cả các nút (Nodes)
        List<GraphNodeDTO> nodes = topicRepo.findBySubject(subject)
                .stream()
                .map(topic -> {
                    double score = masteryMap.getOrDefault(topic.getTopicId(), 0.0);

                    // --- BẮT ĐẦU TÍNH TOÁN METADATA MỚI ---
                    Integer topicId = topic.getTopicId();
                    // Đếm số lượng Quiz Questions có Topic này
                    int quizCount = (int) quizQuestionsRepo.countByTopic_TopicId(topicId);
                    // Đếm số lượng Flashcards có Topic này
                    int flashcardCount = (int) flashcardRepo.countByTopic_TopicId(topicId);

                    long totalAttempts = masteryMap.containsKey(topicId) ? 1 : 0; // Đơn giản hóa
                    String group = getMasteryGroup(score, totalAttempts);

                    // Trả về GraphNodeDTO mới
                    return new GraphNodeDTO(
                            topicId,
                            topic.getTopicName(),
                            group,
                            quizCount,
                            flashcardCount
                    );
                    // --- KẾT THÚC TÍNH TOÁN METADATA MỚI ---
                })
                .collect(Collectors.toList());

        // 3. Lấy tất cả các cạnh (Edges)
        List<GraphEdgeDTO> edges = dependencyRepo.findByTopic_Subject_SubjectId(subject.getSubjectId())
                .stream()
                .map(dep -> new GraphEdgeDTO(dep.getDependsOnTopic().getTopicId(), dep.getTopic().getTopicId()))
                .collect(Collectors.toList());

        KnowledgeMapDTO mapDTO = new KnowledgeMapDTO();
        mapDTO.setNodes(nodes);
        mapDTO.setEdges(edges);

        return mapDTO;
    }

    private String getMasteryGroup(double score, long totalAttempts) {
        if (totalAttempts == 0) return "not_started"; // Xám
        if (score >= 0.8) return "mastered";      // Xanh lá
        if (score >= 0.5) return "learning";      // Vàng
        return "struggling";  // Đỏ
    }
}