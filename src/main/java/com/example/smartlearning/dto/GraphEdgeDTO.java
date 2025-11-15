// Đặt tại: src/main/java/com/example/smartlearning/dto/GraphEdgeDTO.java
package com.example.smartlearning.dto;

import lombok.Data;

@Data
public class GraphEdgeDTO {
    private Integer from; // ID của "cha"
    private Integer to;   // ID của "con"

    public GraphEdgeDTO(Integer from, Integer to) {
        this.from = from;
        this.to = to;
    }
}