package com.example.smartlearning.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PDFService {
	// -------------------------------------------------------------------
    // 1) Đọc PDF → String
    // -------------------------------------------------------------------
    public String extractTextFromPdf(MultipartFile file) {
        try (InputStream is = file.getInputStream(); PDDocument doc = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        } catch (Exception e) {
            throw new RuntimeException("Không đọc được PDF: " + e.getMessage());
        }
    }


    // -------------------------------------------------------------------
    // 2) Ước lượng số token (tương đối chính xác)
    // -------------------------------------------------------------------
    public int estimateTokens(String text) {
        return (int) Math.ceil(text.length() / 3.2);  // 3.2 ký tự/token
    }


    // -------------------------------------------------------------------
    // 3) Chia text PDF thành nhiều chunk nhỏ theo token
    // -------------------------------------------------------------------
    public List<String> splitTextIntoChunks(String text, int maxTokens) {

        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n");

        StringBuilder current = new StringBuilder();
        int currentTokens = 0;

        for (String p : paragraphs) {
            int tokens = estimateTokens(p);

            if (currentTokens + tokens > maxTokens) {
                chunks.add(current.toString());
                current = new StringBuilder();
                currentTokens = 0;
            }

            current.append(p).append("\n");
            currentTokens += tokens;
        }

        if (currentTokens > 0) {
            chunks.add(current.toString());
        }

        return chunks;
    }


}
