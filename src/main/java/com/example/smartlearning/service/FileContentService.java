// Đặt tại: src/main/java/com/example/smartlearning/service/FileContentService.java
package com.example.smartlearning.service;

import org.springframework.beans.factory.annotation.Autowired; // <-- THÊM MỚI
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class FileContentService {

    // --- BẮT ĐẦU SỬA LỖI ---
    @Autowired
    private PDFService pdfService; // Tiêm PDFService vào

    /**
     * Trích xuất văn bản từ bất kỳ file nào (PDF, TXT, v.v.)
     */
    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            return null; // Không thể xác định loại file
        }

        try {
            // KỊCH BẢN 1: Nếu là file PDF
            if (fileName.toLowerCase().endsWith(".pdf")) {
                System.out.println("FileContentService: Phát hiện file PDF, đang dùng PDFService...");
                return pdfService.extractTextFromPdf(file);
            }

            // KỊCH BẢN 2: Nếu là file .txt
            if (fileName.toLowerCase().endsWith(".txt")) {
                System.out.println("FileContentService: Phát hiện file TXT, đang đọc thô...");
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }

            // KỊCH BẢN 3: Các file khác (.docx, .pptx)
            // (Bạn cần thêm thư viện Apache POI để đọc các file này)
            if (fileName.toLowerCase().endsWith(".docx") || fileName.toLowerCase().endsWith(".pptx")) {
                System.err.println("FileContentService: Loại file .docx/.pptx chưa được hỗ trợ. Bỏ qua file.");
                return null; // Trả về null để AI biết
            }

            // KỊCH BẢN 4: File lạ (có thể là ảnh)
            System.err.println("FileContentService: Không hỗ trợ loại file: " + fileName);
            return null;

        } catch (Exception e) { // Bắt cả IOException và RuntimeException từ PDFService
            System.err.println("Lỗi nghiêm trọng khi trích xuất file: " + e.getMessage());
            return null;
        }
    }
    // --- KẾT THÚC SỬA LỖI ---
}
