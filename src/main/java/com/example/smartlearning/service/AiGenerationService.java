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

    @Value("${google.gemini.api.key}")
    private String geminiApiKey;

    @Value("${google.gemini.api.model}")
    private String aiModelUsed;

    @Autowired
    PDFService pdfService;

    private final ObjectMapper mapper = new ObjectMapper();

    private Client geminiClient;

    @PostConstruct
    public void init() {
        this.geminiClient = Client.builder()
                .apiKey(this.geminiApiKey)
                .build();
    }

    public String getAiModelUsed() {
        return aiModelUsed;
    }

    private String extractJsonString(String rawText) {
        if (rawText == null) return null;
        return rawText.trim();
    }

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
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("text/plain")
                    .build();

            GenerateContentResponse response = this.geminiClient.models
                    .generateContent(this.aiModelUsed, userPrompt, config);

            String content = response.text();

            if (content != null && !content.isEmpty()) {
                return content;
            } else {
                throw new RuntimeException("Gemini response is empty or invalid.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Gemini API (StudyPlan) bằng SDK: " + e.getMessage());
            String safeErrorMessage = e.getMessage().replace("\"", "'").replace("'", "`");
            return "# Lộ trình học " + subject.getSubjectName() + " (Mock Fallback)\n\n" +
                    "**API Lỗi:**\n" +
                    "* " + safeErrorMessage;
        }
    }


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

        String userPrompt;
        boolean hasFile = lectureText != null && !lectureText.isBlank();
        boolean hasTopic = topic != null && !topic.isBlank();

        if (hasFile && hasTopic) {
            userPrompt = String.format(
                    "Bạn là một AI chuyên tạo câu hỏi quiz từ tài liệu. " +
                            "Dưới đây là một tài liệu: [BEGIN_DOCUMENT]%s[END_DOCUMENT]. " +
                            "VÀ MỘT CHỦ ĐỀ: '%s'. " +
                            "HÃY LÀM THEO CÁC BƯỚC SAU: " +
                            "1. Đọc tài liệu và xác định xem chủ đề '%s' có được đề cập rõ ràng trong tài liệu không. " +
                            "2. NẾU KHÔNG TÌM THẤY CHỦ ĐỀ, hãy trả về một chuỗi duy nhất: 'ERROR_TOPIC_NOT_FOUND'. " +
                            "3. NẾU TÌM THẤY CHỦ ĐỀ, hãy tạo %d câu hỏi trắc nghiệm (A, B, C, D) CHỈ DỰA TRÊN TÀI LIỆU, tập trung vào chủ đề đó.",
                    lectureText,
                    topic,
                    topic,
                    numQuestions
            );
        } else if (hasFile && !hasTopic) {
            userPrompt = String.format(
                    "Bạn là một AI chuyên tạo câu hỏi quiz từ tài liệu. " +
                            "Dưới đây là một tài liệu: [BEGIN_DOCUMENT]%s[END_DOCUMENT]. " +
                            "Hãy đọc toàn bộ tài liệu và tạo %d câu hỏi trắc nghiệm (A, B, C, D) " +
                            "dựa trên nội dung tổng quan của tài liệu.",
                    lectureText,
                    numQuestions
            );
        } else {
            userPrompt = String.format(
                    "Chủ đề quiz: %s (cho môn %s).\n" +
                            "Phong cách học của sinh viên: %s.",
                    (topic != null ? topic : "Tổng quan môn học"),
                    subject.getSubjectName(),
                    user.getLearningStyle()
            );
        }

        System.out.println("--- GỬI PROMPT TỚI GEMINI (QUIZ) bằng SDK ---");

        try {
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = this.geminiClient.models
                    .generateContent(this.aiModelUsed, userPrompt, config);

            String jsonContent = response.text();

            if (jsonContent != null && !jsonContent.isEmpty()) {
                return jsonContent;
            } else {
                throw new RuntimeException("Gemini response is empty or invalid (JSON).");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Gemini API (Quiz) bằng SDK: " + e.getMessage());
            System.err.println("Sử dụng dữ liệu Quiz giả lập (Mock)");

            String safeErrorMessage = e.getMessage().replace("\"", "'").replace("'", "`");
            return "[ \n" +
                    "  { \n" +
                    "    \"questionText\": \"Câu hỏi giả lập 1: API gặp lỗi\", \n" +
                    "    \"options\": {\"A\": \"Đáp án A\", \"B\": \"Đáp án B\", \"C\": \"Đáp án C\", \"D\": \"Đáp án D\"}, \n" +
                    "    \"correctAnswer\": \"A\", \n" +
                    "    \"explanation\": \"Đây là dữ liệu giả lập do không thể gọi API. Lỗi: " + safeErrorMessage + "\" \n" +
                    "  } \n" +
                    "]";
        }
    }


    public String generateFlashcards(User user, Subject subject, String topic, int numCards) {
        String systemPrompt = String.format(
                "Bạn là một trợ lý học tập, chuyên tạo flashcards. Hãy tạo %d flashcard.\n" +
                        "Luôn luôn trả về kết quả dưới dạng một mảng (array) JSON. KHÔNG dùng markdown.\n" +
                        "Định dạng JSON cho mỗi đối tượng trong mảng phải là:\n" +
                        "{\n" +
                        "  \"frontText\": \"Mặt trước (Thuật ngữ / Câu hỏi)\",\n" +
                        "  \"backText\": \"Mặt sau (Định nghĩa / Trả lời)\"\n" +
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
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = this.geminiClient.models
                    .generateContent(this.aiModelUsed, userPrompt, config);

            String jsonContent = response.text();

            if (jsonContent != null && !jsonContent.isEmpty()) {
                return jsonContent;
            } else {
                throw new RuntimeException("Gemini response is empty or invalid (JSON).");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Gemini API (Flashcard) bằng SDK: " + e.getMessage());
            System.err.println("Sử dụng dữ liệu Flashcard giả lập (Mock)");

            String safeErrorMessage = e.getMessage().replace("\"", "'").replace("'", "`");
            return "[ \n" +
                    "  { \n" +
                    "    \"frontText\": \"Flashcard giả lập (Lỗi API)\", \n" +
                    "    \"backText\": \"Không thể tạo flashcard. Lỗi: " + safeErrorMessage + "\" \n" +
                    "  } \n" +
                    "]";
        }
    }


    public List<FlashcardDTO> generateFlashcardsForChunk(String chunk, int numCards) {

        String systemPrompt =
                "Bạn là trợ lý tạo flashcard. Hãy tạo đúng " + numCards + " flashcards.\n" +
                        "Luôn trả về kết quả dưới dạng JSON Array.\n" +
                        "Ví dụ: [{\"front\":\"...\", \"back\":\"...\"}]";
        String userPrompt =
                "Tạo " + numCards + " flashcards dựa trên nội dung:\n\n" +
                        chunk;

        try {
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = this.geminiClient.models
                    .generateContent(this.aiModelUsed, userPrompt, config);

            String json = response.text();

            return mapper.readValue(json, new TypeReference<List<FlashcardDTO>>() {});
        } catch (Exception e) {
            System.err.println("Lỗi Gemini (Chunk PDF) bằng SDK: " + e.getMessage());
            return new ArrayList<>();
        }
    }

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