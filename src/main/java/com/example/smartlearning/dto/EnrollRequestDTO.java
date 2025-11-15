package com.example.smartlearning.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollRequestDTO {
    @NotNull
    private Integer userId;
    @NotNull
    private Integer subjectId;
}