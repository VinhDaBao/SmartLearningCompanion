package com.example.smartlearning.service;

import com.example.smartlearning.dto.FlashcardDTO;
// MỚI: Thêm các import bị thiếu
import com.example.smartlearning.dto.ai.ChatMessageDTO;
import com.example.smartlearning.dto.ai.OpenAiRequestDTO;
import com.example.smartlearning.dto.ai.OpenAiResponseDTO;
import com.example.smartlearning.model.Flashcard;
import com.example.smartlearning.model.Subject;
import com.example.smartlearning.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiGenerationService {

    // --- PHẦN BỊ THIẾU MÀ TÔI ĐÃ BỔ SUNG ---
    @Autowired
    private RestTemplate restTemplate; // Bean để gọi API

    @Value("${openai.api.key}")
    private String openApiKey; // Key từ application.properties

    @Value("${openai.api.url}")
    private String openApiUrl; // URL từ application.properties
    // --- KẾT THÚC BỔ SUNG ---

    /**
     * Tạo một Lộ trình học (Study Plan) bằng cách gọi AI.
     * (TÔI ĐÃ SỬA LẠI HÀM NÀY ĐỂ GỌI API THẬT, thay vì chỉ mock)
     */
    public String generateStudyPlan(User user, Subject subject, String customPrompt) {

        // 1. Tạo Prompt
        String systemPrompt = "Bạn là một trợ lý học tập thông minh. Hãy tạo một lộ trình học chi tiết. Hãy trả về kết quả dưới dạng Markdown.";
        String userPrompt = String.format(
                "Môn học: '%s'.\n" +
                        "Mô tả: %s\n" +
                        "Phong cách học của sinh viên: %s.\n" +
                        "Yêu cầu thêm: %s",
                subject.getSubjectName(),
                subject.getDescription(),
                user.getLearningStyle(),
                (customPrompt != null ? customPrompt : "Không có")
        );

        System.out.println("--- GỬI PROMPT TỚI AI (StudyPlan) ---");
        System.out.println(systemPrompt + "\n" + userPrompt);

        // 2. GỌI API BÊN NGOÀI (OpenAI)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openApiKey);

        OpenAiRequestDTO requestBody = new OpenAiRequestDTO(
                "gpt-4o-mini",
                List.of(
                        new ChatMessageDTO("system", systemPrompt),
                        new ChatMessageDTO("user", userPrompt)
                )
        );

        HttpEntity<OpenAiRequestDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            OpenAiResponseDTO response = restTemplate.postForObject(
                    openApiUrl, httpEntity, OpenAiResponseDTO.class
            );

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            } else {
                throw new RuntimeException("AI response is empty or invalid.");
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi OpenAI API (StudyPlan): " + e.getMessage());
            // Trả về mock data nếu API lỗi
            return "# Lộ trình học " + subject.getSubjectName() + " (Mock Fallback)\n\n" +
                    "**Tuần 1: Giới thiệu**\n" +
                    "* API call failed. This is mock data.";
        }
    }

    /**
     * Tạo một bộ Quiz bằng cách gọi AI.
     * (Code của bạn ở đây đã OK, chỉ thiếu các trường khai báo)
     */
    public String generateQuiz(User user, Subject subject, String topic, int numQuestions) {

        // 1. Tạo Prompt
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

        System.out.println("--- GỬI PROMPT TỚI AI (QUIZ) ---");
        System.out.println(systemPrompt + "\n" + userPrompt);

        // 2. GỌI API BÊN NGOÀI (OpenAI)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openApiKey);

        OpenAiRequestDTO requestBody = new OpenAiRequestDTO(
                "gpt-4o-mini",
                List.of(
                        new ChatMessageDTO("system", systemPrompt),
                        new ChatMessageDTO("user", userPrompt)
                )
        );

        HttpEntity<OpenAiRequestDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            OpenAiResponseDTO response = restTemplate.postForObject(
                    openApiUrl, httpEntity, OpenAiResponseDTO.class
            );

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            } else {
                throw new RuntimeException("AI response is empty or invalid.");
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi OpenAI API (Quiz): " + e.getMessage());
            System.err.println("Sử dụng dữ liệu Quiz giả lập (Mock)");
            return "[ \n" +
                    "  { \n" +
                    "    \"questionText\": \"Câu hỏi giả lập 1: Spring Boot là gì?\", \n" +
                    "    \"options\": {\"A\": \"Framework\", \"B\": \"Thư viện\", \"C\": \"Ứng dụng\", \"D\": \"Ngôn ngữ\"}, \n" +
                    "    \"correctAnswer\": \"A\", \n" +
                    "    \"explanation\": \"Spring Boot là một framework. (Đây là mock data)\" \n" +
                    "  }, \n" +
                    "  { \n" +
                    "    \"questionText\": \"Câu hỏi giả lập 2: @RestController dùng để làm gì?\", \n" +
                    "    \"options\": {\"A\": \"Service\", \"B\": \"Component\", \"C\": \"API Controller\", \"D\": \"Entity\"}, \n" +
                    "    \"correctAnswer\": \"C\", \n" +
                    "    \"explanation\": \"Nó kết hợp @Controller và @ResponseBody. (Đây là mock data)\" \n" +
                    "  } \n" +
                    "]";
        }
    }

    // --- LỖI CÚ PHÁP Ở ĐÂY ---
    // (Tôi đã xóa Javadoc lỗi và dấu '*/' bị thừa của bạn ở đây)

    /**
     * Tạo một bộ Flashcard bằng cách gọi AI.
     * @return Một chuỗi JSON (String) chứa danh sách các thẻ.
     */
    public String generateFlashcards(User user, Subject subject, String topic, int numCards) {

        // 1. Tạo Prompt
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

        // 2. GỌI API BÊN NGOÀI (OpenAI)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openApiKey);

        OpenAiRequestDTO requestBody = new OpenAiRequestDTO(
                "gpt-4o-mini",
                List.of(
                        new ChatMessageDTO("system", systemPrompt),
                        new ChatMessageDTO("user", userPrompt)
                )
        );

        HttpEntity<OpenAiRequestDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            OpenAiResponseDTO response = restTemplate.postForObject(
                    openApiUrl, httpEntity, OpenAiResponseDTO.class
            );

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            } else {
                throw new RuntimeException("AI response is empty or invalid.");
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi OpenAI API (Flashcard): " + e.getMessage());
            System.err.println("Sử dụng dữ liệu Flashcard giả lập (Mock)");
            return "[ \n" +
                    "  { \n" +
                    "    \"front\": \"@Service (Mock)\", \n" +
                    "    \"back\": \"Một annotation đánh dấu lớp logic nghiệp vụ. (Mock)\" \n" +
                    "  }, \n" +
                    "  { \n" +
                    "    \"front\": \"@Autowired (Mock)\", \n" +
                    "    \"back\": \"Annotation dùng để tiêm (inject) dependency. (Mock)\" \n" +
                    "  } \n" +
                    "]";
        }
    }
    
    @Autowired
    PDFService pdfService;

    private final ObjectMapper mapper = new ObjectMapper();
    public List<FlashcardDTO> generateFlashcardsForChunk(String chunk, int numCards) {

        String systemPrompt =
                "Bạn là trợ lý tạo flashcard. Hãy tạo đúng số lượng flashcards yêu cầu.\n" +
                "Luôn trả về kết quả dưới dạng JSON Array.\n" +
                "Ví dụ: [{\"front\":\"...\", \"back\":\"...\"}]";

        String userPrompt =
                "Tạo " + numCards + " flashcards dựa trên nội dung:\n\n" +
                chunk;


        // Tùy vào DTO của bạn (mình viết cấu trúc chuẩn)
        ChatMessageDTO msgSystem = new ChatMessageDTO("system", systemPrompt);
        ChatMessageDTO msgUser = new ChatMessageDTO("user", userPrompt);

        OpenAiRequestDTO request = new OpenAiRequestDTO(
                "gpt-4o-mini",
                List.of(msgSystem, msgUser)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openApiKey);

        HttpEntity<OpenAiRequestDTO> entity = new HttpEntity<>(request, headers);

        try {
            OpenAiResponseDTO response =
                    restTemplate.postForObject(openApiUrl, entity, OpenAiResponseDTO.class);

            String json = response.getChoices().get(0).getMessage().getContent();

            return mapper.readValue(json, new TypeReference<List<FlashcardDTO>>() {});

        } catch (Exception e) {
            throw new RuntimeException("Lỗi OpenAI: " + e.getMessage());
        }
    }
    public List<FlashcardDTO> generateFlashcardsFromLargePdf(
            MultipartFile pdfFile,
            int totalCards
    ) {

        String text = pdfService.extractTextFromPdf(pdfFile);

        List<String> chunks = pdfService.splitTextIntoChunks(text, 10000);

        int cardsPerChunk = Math.max(3, totalCards / chunks.size());

        List<FlashcardDTO> allCards = new ArrayList<>();

        for (String chunk : chunks) {
            List<FlashcardDTO> part = generateFlashcardsForChunk(chunk, cardsPerChunk);
            allCards.addAll(part);
        }

        return allCards;
    }

}