package com.example.smartlearning.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class GoogleSearchRequestDTO {
    private List<String> queries;
}