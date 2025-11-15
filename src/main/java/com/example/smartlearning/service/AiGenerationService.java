package com.example.smartlearning.service;

import com.example.smartlearning.dto.ai.ChatMessageDTO;
import com.example.smartlearning.dto.ai.OpenAiRequestDTO;
import com.example.smartlearning.dto.ai.OpenAiResponseDTO;
import com.example.smartlearning.model.Subject;
import com.example.smartlearning.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AiGenerationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openApiKey;

    @Value("${openai.api.url}")
    private String openApiUrl;

    @Value("${openai.api.model}")
    private String aiModelUsed;

    public String getAiModelUsed() {
        return aiModelUsed;
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
                    "\nDưới đây là nội dung bài giảng / slide mà giảng viên cung cấp. " +
                            "Hãy dựa sát vào nội dung này để xây dựng lộ trình học, chia theo buổi/tuần cho hợp lý:\n"
            );
            userPromptBuilder.append(lectureText);
        }

        String userPrompt = userPromptBuilder.toString();

        return callOpenAi(systemPrompt, userPrompt);
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

        return callOpenAi(systemPrompt, userPrompt);
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

        return callOpenAi(systemPrompt, userPrompt);
    }

    private String callOpenAi(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openApiKey);

        OpenAiRequestDTO requestBody = new OpenAiRequestDTO(
                aiModelUsed,
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
                String content = response.getChoices().get(0).getMessage().getContent();
                return content.trim();
            } else {
                throw new RuntimeException("AI response is empty or invalid.");
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi gọi OpenAI API: " + e.getMessage());
            return "Lỗi: " + e.getMessage();
        }
    }
}