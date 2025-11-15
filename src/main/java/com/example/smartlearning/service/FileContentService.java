package com.example.smartlearning.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class FileContentService {
    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Lỗi đọc file bài giảng: " + e.getMessage());
            return null;
        }
    }
}
