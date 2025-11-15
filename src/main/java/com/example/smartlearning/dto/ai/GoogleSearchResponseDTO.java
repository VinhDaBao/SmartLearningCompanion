package com.example.smartlearning.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class GoogleSearchResponseDTO {
    private List<SearchResultItem> results;

    @Data
    public static class SearchResultItem {
        private String url;
        private String source_title;
    }
}