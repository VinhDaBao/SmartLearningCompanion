package com.example.smartlearning.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import com.example.smartlearning.dto.FlashcardDTO;
import com.example.smartlearning.model.Subject;
import com.example.smartlearning.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiGenerationService {

    // XÓA RestTemplate
    // @Autowired
    // private RestTemplate restTemplate;

    @Value("${google.gemini.api.key}")
    private String geminiApiKey;

    // XÓA URL API
    // @Value("${google.gemini.api.url}")
    // private String geminiApiUrl;

    @Autowired
    PDFService pdfService;

    // Vẫn cần ObjectMapper để parse JSON mock và cho hàm PDF
    private final ObjectMapper mapper = new ObjectMapper();

    // THÊM CLIENT CỦA SDK
    private Client geminiClient;

    // THÊM HÀM INIT ĐỂ KHỞI TẠO CLIENT
    @PostConstruct
    public void init() {
        // Khởi tạo Client bằng API Key từ application.properties
        this.geminiClient = Client.builder()
                .apiKey(this.geminiApiKey)
                .build();
    }

    // HÀM HELPER (Giữ lại để parse JSON trả về từ mock)
    // Hàm extractJsonString không còn cần thiết cho SDK, nhưng
    // hàm generateFlashcardsForChunk vẫn dùng mapper nên ta giữ lại
    private String extractJsonString(String rawText) {
        if (rawText == null) return null;
        // SDK trả về text sạch, không cần replace ```json
        // Chỉ cần trim()
        return rawText.trim();
    }


    /**
     * Tạo một Lộ trình học (Study Plan) - Cập nhật dùng SDK
     */
    public String generateStudyPlan(User user,
                                    Subject subject,
                                    String customPrompt,
                                    String lectureText) {

        String systemPrompt = "Bạn là một trợ lý học tập thông minh. Hãy tạo một lộ trình học chi tiết. Hãy trả về kết quả dưới dạng Markdown.";

        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append(
                String.format(
                        "Môn học: '%s'.\nMô tả: %s\nPhong cách học của sinh viên: %s.\n",
                        subject.getSubjectName(),
                        subject.getDescription(),
                        user.getLearningStyle()
                )
        );
        if (customPrompt != null && !customPrompt.isBlank()) {
            userPromptBuilder.append("Yêu cầu thêm từ sinh viên: ").append(customPrompt).append("\n");
        }
        if (lectureText != null && !lectureText.isBlank()) {
            userPromptBuilder.append(
                    "\nDưới đây là nội dung bài giảng / slide, hãy dựa vào đây để xây dựng lộ trình:\n"
            );
            userPromptBuilder.append(lectureText);
        }
        String userPrompt = userPromptBuilder.toString();

        System.out.println("--- GỬI PROMPT TỚI GEMINI (StudyPlan) bằng SDK ---");

        try {
            // 1. Tạo System Instruction
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));

            // 2. Tạo Config (Markdown là text/plain)
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("text/plain") // Yêu cầu text
                    .build();

            // 3. Gọi API
            // Dùng model mới "gemini-2.5-flash" theo tài liệu
            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", userPrompt, config);

            // 4. Lấy text
            String content = response.text();

            if (content != null && !content.isEmpty()) {
                return content;
            } else {
                throw new RuntimeException("Gemini response is empty or invalid.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Gemini API (StudyPlan) bằng SDK: " + e.getMessage());
            // SỬA LỖI MOCK DATA: Trả về text hợp lệ
            return "# Lộ trình học " + subject.getSubjectName() + " (Mock Fallback)\n\n" +
                    "**API Lỗi:**\n" +
                    "* " + e.getMessage().replace("\"", "'").replace("'", "`"); // Làm sạch lỗi
        }
    }


    /**
     * Tạo một bộ Quiz - Cập nhật dùng SDK (Ép kiểu JSON)
     */
    public String generateQuiz(User user, Subject subject, String topic, int numQuestions, String lectureText) {

        String systemPrompt = String.format(
                "Bạn là một trợ lý học tập, chuyên tạo câu hỏi quiz. Hãy tạo %d câu hỏi trắc nghiệm.\n" +
                        "Luôn luôn trả về kết quả dưới dạng một mảng (array) JSON. KHÔNG dùng markdown.\n" +
                        "Định dạng JSON cho mỗi đối tượng trong mảng phải là:\n" +
                        "{\n" +
                        "  \"questionText\": \"Nội dung câu hỏi\",\n" +
                        "  \"options\": {\"A\": \"Lựa chọn A\", \"B\": \"Lựa chọn B\", \"C\": \"Lựa chọn C\", \"D\": \"Lựa chọn D\"},\n" +
                        "  \"correctAnswer\": \"A\",\n" +
                        "  \"explanation\": \"Giải thích tại sao đáp án A đúng.\"\n" +
                        "}", numQuestions
        );
        String userPrompt = String.format(
                "Chủ đề quiz: %s (cho môn %s).\n" +
                        "Phong cách học của sinh viên: %s.",
                (topic != null ? topic : "Tổng quan môn học"),
                subject.getSubjectName(),
                user.getLearningStyle()
        );

        System.out.println("--- GỬI PROMPT TỚI GEMINI (QUIZ) bằng SDK ---");

        try {
            // 1. Tạo System Instruction
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));

            // 2. Tạo Config (Ép kiểu JSON)
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("application/json") // Yêu cầu JSON
                    .build();

            // 3. Gọi API
            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", userPrompt, config);

            // 4. Lấy text (SDK đã đảm bảo đây là JSON sạch)
            String jsonContent = response.text();

            if (jsonContent != null && !jsonContent.isEmpty()) {
                return jsonContent;
            } else {
                throw new RuntimeException("Gemini response is empty or invalid (JSON).");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Gemini API (Quiz) bằng SDK: " + e.getMessage());
            System.err.println("Sử dụng dữ liệu Quiz giả lập (Mock)");

            // --- SỬA LỖI MOCK DATA (HẾT CRASH) ---
            String safeErrorMessage = e.getMessage().replace("\"", "'").replace("'", "`");
            return "[ \n" +
                    "  { \n" +
                    "    \"questionText\": \"Câu hỏi giả lập 1: API gặp lỗi\", \n" +
                    "    \"options\": {\"A\": \"Đáp án A\", \"B\": \"Đáp án B\", \"C\": \"Đáp án C\", \"D\": \"Đáp án D\"}, \n" +
                    "    \"correctAnswer\": \"A\", \n" +
                    "    \"explanation\": \"Đây là dữ liệu giả lập do không thể gọi API. Lỗi: " + safeErrorMessage + "\" \n" +
                    "  } \n" +
                    "]";
            // --- KẾT THÚC SỬA LỖI ---
        }
    }


    /**
     * Tạo một bộ Flashcard - Cập nhật dùng SDK (Ép kiểu JSON)
     */
    public String generateFlashcards(User user, Subject subject, String topic, int numCards) {
        String systemPrompt = String.format(
                "Bạn là một trợ lý học tập, chuyên tạo flashcards. Hãy tạo %d flashcard.\n" +
                        "Luôn luôn trả về kết quả dưới dạng một mảng (array) JSON. KHÔNG dùng markdown.\n" +
                        "Định dạng JSON cho mỗi đối tượng trong mảng phải là:\n" +
                        "{\n" +
                        "  \"front\": \"Mặt trước (Thuật ngữ / Câu hỏi)\",\n" +
                        "  \"back\": \"Mặt sau (Định nghĩa / Trả lời)\"\n" +
                        "}", numCards
        );
        String userPrompt = String.format(
                "Chủ đề: %s (cho môn %s).\n" +
                        "Phong cách học của sinh viên: %s.\n" +
                        "Tập trung vào các thuật ngữ và định nghĩa quan trọng.",
                (topic != null ? topic : "Tổng quan môn học"),
                subject.getSubjectName(),
                user.getLearningStyle()
        );

        System.out.println("--- GỬI PROMPT TỚI GEMINI (Flashcard) bằng SDK ---");

        try {
            // 1. Tạo System Instruction
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));

            // 2. Tạo Config (Ép kiểu JSON)
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("application/json") // Yêu cầu JSON
                    .build();

            // 3. Gọi API
            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", userPrompt, config);

            // 4. Lấy text (SDK đã đảm bảo đây là JSON sạch)
            String jsonContent = response.text();

            if (jsonContent != null && !jsonContent.isEmpty()) {
                return jsonContent;
            } else {
                throw new RuntimeException("Gemini response is empty or invalid (JSON).");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Gemini API (Flashcard) bằng SDK: " + e.getMessage());
            System.err.println("Sử dụng dữ liệu Flashcard giả lập (Mock)");

            // --- SỬA LỖI MOCK DATA (HẾT CRASH) ---
            String safeErrorMessage = e.getMessage().replace("\"", "'").replace("'", "`");
            return "[ \n" +
                    "  { \n" +
                    "    \"front\": \"Flashcard giả lập (Lỗi API)\", \n" +
                    "    \"back\": \"Không thể tạo flashcard. Lỗi: " + safeErrorMessage + "\" \n" +
                    "  } \n" +
                    "]";
            // --- KẾT THÚC SỬA LỖI ---
        }
    }


    /**
     * Tạo Flashcard từ một phần text (chunk) - Cập nhật dùng SDK
     */
    public List<FlashcardDTO> generateFlashcardsForChunk(String chunk, int numCards) {

        String systemPrompt =
                "Bạn là trợ lý tạo flashcard. Hãy tạo đúng " + numCards + " flashcards.\n" +
                        "Luôn trả về kết quả dưới dạng JSON Array.\n" +
                        "Ví dụ: [{\"front\":\"...\", \"back\":\"...\"}]";
        String userPrompt =
                "Tạo " + numCards + " flashcards dựa trên nội dung:\n\n" +
                        chunk;

        try {
            // 1. Tạo System Instruction
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));

            // 2. Tạo Config (Ép kiểu JSON)
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("application/json") // Yêu cầu JSON
                    .build();

            // 3. Gọi API
            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", userPrompt, config);

            // 4. Lấy text (SDK đã đảm bảo đây là JSON sạch)
            String json = response.text();

            // 5. Dùng ObjectMapper để chuyển JSON string thành List<FlashcardDTO>
            return mapper.readValue(json, new TypeReference<List<FlashcardDTO>>() {});
        } catch (Exception e) {
            System.err.println("Lỗi Gemini (Chunk PDF) bằng SDK: " + e.getMessage());
            // Trả về danh sách rỗng
            return new ArrayList<>();
        }
    }

    /**
     * Hàm này không cần sửa (vẫn giữ nguyên)
     * Vì nó gọi đến hàm generateFlashcardsForChunk đã được sửa ở trên
     */
    public List<FlashcardDTO> generateFlashcardsFromLargePdf(
            MultipartFile pdfFile,
            int totalCards
    ) {
        String text = pdfService.extractTextFromPdf(pdfFile);
        List<String> chunks = pdfService.splitTextIntoChunks(text, 10000);
        int cardsPerChunk = Math.max(3, (chunks.isEmpty() ? totalCards : totalCards / chunks.size()));
        List<FlashcardDTO> allCards = new ArrayList<>();
        for (String chunk : chunks) {
            List<FlashcardDTO> part = generateFlashcardsForChunk(chunk, cardsPerChunk);
            allCards.addAll(part);
        }
        return allCards;
    }
}