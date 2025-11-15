// Đặt tại: src/main/java/com/example/smartlearning/dto/KnowledgeMapDTO.java
package com.example.smartlearning.dto;

import lombok.Data;
import java.util.List;

@Data
public class KnowledgeMapDTO {
    private List<GraphNodeDTO> nodes;
    private List<GraphEdgeDTO> edges;
}