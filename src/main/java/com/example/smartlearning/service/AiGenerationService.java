package com.example.smartlearning.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import com.example.smartlearning.dto.FlashcardDTO;
import com.example.smartlearning.dto.ai.VideoSuggestionDTO;
import com.example.smartlearning.model.Subject;
import com.example.smartlearning.model.SubjectContent;
import com.example.smartlearning.model.Topic;
import com.example.smartlearning.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    .generateContent("gemini-2.5-flash", userPrompt, config);

            String content = response.text();

            if (content != null && !content.isEmpty()) {
                return content;
            } else {
                throw new RuntimeException("Gemini response is empty or invalid.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Gemini API (StudyPlan) bằng SDK: " + e.getMessage());
            return "# Lộ trình học " + subject.getSubjectName() + " (Mock Fallback)\n\n" +
                    "**API Lỗi:**\n" +
                    "* " + e.getMessage().replace("\"", "'").replace("'", "`");
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
                        "  \"explanation\": \"Giải thích tại sao đáp án A đúng.\",\n" +
                        "  \"topicName\": \"Tên chủ đề chính của câu hỏi này (ví dụ: Spring Data JPA, Dependency Injection)\"\n" +
                        "}", numQuestions
        );


        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append(String.format(
                "Môn học: %s.\n" +
                        "Chủ đề quiz: %s.\n" +
                        "Phong cách học của sinh viên: %s.\n",
                subject.getSubjectName(),
                (topic != null ? topic : "Tổng quan môn học"),
                user.getLearningStyle()
        ));

        if (lectureText != null && !lectureText.isBlank()) {
            userPromptBuilder.append(
                    "\nDưới đây là nội dung tài liệu / bài giảng. HÃY DỰA VÀO NỘI DUNG NÀY ĐỂ TẠO CÂU HỎI:\n\n"
            );
            userPromptBuilder.append("--- NỘI DUNG BẮT ĐẦU ---\n");
            userPromptBuilder.append(lectureText);
            userPromptBuilder.append("\n--- NỘI DUNG KẾT THÚC ---");
        }

        String userPrompt = userPromptBuilder.toString();

        System.out.println("--- GỬI PROMPT TỚI GEMINI (QUIZ) bằng SDK ---");
        System.out.println("**************************************************");
        System.out.println("NỘI DUNG FILE MÀ AI NHẬN ĐƯỢC (lectureText):");
        System.out.println(lectureText != null ? lectureText : "!!! LỖI: NỘI DUNG FILE BỊ NULL !!!");
        System.out.println("**************************************************");

        try {
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", userPrompt, config);

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
                    "    \"explanation\": \"Đây là dữ liệu giả lập... Lỗi: " + safeErrorMessage + "\", \n" +
                    "    \"topicName\": \"Chủ đề Lỗi\" \n" +
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
                        "  \"front\": \"Mặt trước (Thuật ngữ / Câu hỏi)\",\n" +
                        "  \"back\": \"Mặt sau (Định nghĩa / Trả lời)\",\n" +
                        "  \"topicName\": \"Tên chủ đề chính của flashcard này (ví dụ: Spring Bean, Interface)\"\n" +
                        "}", numCards
        );

        // --- BẮT ĐẦU SỬA (Giống QuizService) ---
        StringBuilder userPromptBuilder = new StringBuilder();
        userPromptBuilder.append(String.format(
                "Chủ đề: %s (cho môn %s).\n" +
                        "Phong cách học của sinh viên: %s.\n" +
                        "Tập trung vào các thuật ngữ và định nghĩa quan trọng.",
                (topic != null ? topic : "Tổng quan môn học"),
                subject.getSubjectName(),
                user.getLearningStyle()
        ));

        if (lectureText != null && !lectureText.isBlank()) {
            userPromptBuilder.append(
                    "\nDưới đây là nội dung tài liệu / bài giảng. HÃY DỰA VÀO NỘI DUNG NÀY ĐỂ TẠO FLASHCARD:\n\n"
            );
            userPromptBuilder.append("--- NỘI DUNG BẮT ĐẦU ---\n");
            userPromptBuilder.append(lectureText);
            userPromptBuilder.append("\n--- NỘI DUNG KẾT THÚC ---");
        }

        String userPrompt = userPromptBuilder.toString();
        // --- KẾT THÚC SỬA ---


        System.out.println("--- GỬI PROMPT TỚI GEMINI (Flashcard) bằng SDK ---");
        System.out.println("NỘI DUNG FILE MÀ AI NHẬN ĐƯỢC (lectureText): " + (lectureText != null ? "Có nội dung" : "NULL"));


        try {
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", userPrompt, config);

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
                    "    \"front\": \"Flashcard giả lập (Lỗi API)\", \n" +
                    "    \"back\": \"Không thể tạo flashcard. Lỗi: " + safeErrorMessage + "\", \n" +
                    "    \"topicName\": \"Chủ đề Lỗi\" \n" +
                    "  } \n" +
                    "]";
        }
    }

    public VideoSuggestionDTO googleSearchAndSuggestVideo(String topic) {
        System.out.println("--- GỢI Ý VIDEO YOUTUBE CHO: " + topic + " ---");
        String safeTopic = (topic == null || topic.isBlank())
                ? "Java programming tutorial for beginners"
                : topic.trim();

        String searchText = "học " + safeTopic + " cho người mới bắt đầu";
        String encoded = URLEncoder.encode(searchText, StandardCharsets.UTF_8);
        String youtubeSearchUrl = "https://www.youtube.com/results?search_query=" + encoded;

        VideoSuggestionDTO dto = new VideoSuggestionDTO();
        dto.setTitle("Xem video YouTube về: " + searchText);
        dto.setEmbedUrl(youtubeSearchUrl);

        return dto;
    }


    private String extractYouTubeVideoId(String youtubeUrl) {
        String videoId = null;
        Pattern pattern = Pattern.compile("v=([a-zA-Z0-9_-]{11})");
        Matcher matcher = pattern.matcher(youtubeUrl);
        if (matcher.find()) {
            videoId = matcher.group(1);
        }
        return videoId;
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
                    .generateContent("gemini-2.5-flash", userPrompt, config);

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


    /**
     * Tạo văn bản đề xuất thích ứng
     * (Hàm này giữ nguyên)
     */
    public String generateRecommendationText(
            String learningStyle,
            Topic weakestTopic,
            SubjectContent content,
            String scenario
    ) {
        String systemPrompt = "Bạn là một trợ lý học tập AI thân thiện, đưa ra lời khuyên ngắn gọn (1-2 câu).";

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append(String.format("Phong cách học của sinh viên: %s.\n", learningStyle));

        switch (scenario) {
            case "NO_ATTEMPTS":
                userPrompt.append("Sinh viên này CHƯA LÀM BÀI TẬP nào cho môn này. " +
                        "Hãy viết một lời chào mừng và khuyến khích họ bắt đầu học hoặc làm một bài quiz.");
                break;
            case "PERFECT":
                userPrompt.append("Sinh viên này đã làm bài và không có lỗi sai nào. " +
                        "Hãy viết một lời khen ngợi ấn tượng về kết quả hoàn hảo của họ.");
                break;
            case "GENERAL_ERROR":
                userPrompt.append("Sinh viên này có một số lỗi sai ở các bài quiz cũ (chưa được gắn chủ đề). " +
                        "Hãy động viên họ và nhắc họ ôn tập lại kiến thức chung của môn học.");
                break;
            case "WEAK_NO_MATERIAL":
                userPrompt.append(String.format(
                        "Sinh viên này yếu chủ đề '%s', nhưng thư viện không có tài liệu. " +
                                "Hãy động viên họ và đề nghị họ tự ôn tập chủ đề này.",
                        weakestTopic.getTopicName()
                ));
                break;
            case "RECOMMEND":
                userPrompt.append(String.format(
                        "Sinh viên này yếu chủ đề '%s'. " +
                                "Hãy đề xuất họ xem tài liệu sau: [Loại: %s, Tiêu đề: '%s']. " +
                                "Hãy thật thân thiện và khuyến khích.",
                        weakestTopic.getTopicName(),
                        content.getContentType(),
                        content.getTitle()
                ));
                break;
            default:
                userPrompt.append("Hãy đưa ra một lời khuyên học tập chung.");
        }

        try {
            Content systemInstruction = Content.fromParts(Part.fromText(systemPrompt));
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .responseMimeType("text/plain")
                    .build();

            GenerateContentResponse response = this.geminiClient.models
                    .generateContent("gemini-2.5-flash", userPrompt.toString(), config);

            return response.text();

        } catch (Exception e) {
            System.err.println("Lỗi khi tạo text đề xuất: " + e.getMessage());
            // Fallback (dự phòng)
            switch (scenario) {
                case "NO_ATTEMPTS":
                    return "Chào mừng bạn! Hãy bắt đầu làm quiz để AI có thể giúp bạn nhé.";
                case "PERFECT":
                    return "Kết quả của bạn thật ấn tượng! Hãy tiếp tục phát huy.";
                case "RECOMMEND":
                    return String.format("Có vẻ bạn đang gặp khó khăn với chủ đề '%s'. " +
                                    "Hãy thử xem tài liệu này nhé: %s",
                            weakestTopic.getTopicName(), content.getTitle());
                default:
                    return "Hãy cố gắng ôn tập kỹ hơn nhé!";
            }
        }
    }
}