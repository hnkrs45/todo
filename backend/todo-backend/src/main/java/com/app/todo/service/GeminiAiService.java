package com.app.todo.service;

import com.app.todo.dto.request.AiTaskBreakdownRequest;
import com.app.todo.dto.response.AiSubtaskResponse;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public List<AiSubtaskResponse> generateSubtasks(AiTaskBreakdownRequest request) {
        String systemInstruction = String.format(
                "You are an assistant that breaks down a broad goal into %d actionable, concise subtasks. "
                        + "Respond ONLY with a valid JSON array of objects with keys 'title' and 'description'. "
                        + "Do not include markdown ticks, backticks, or extra explanation.",
                request.getNumberOfTasks()
        );

        String prompt = "Goal: " + request.getGoal();

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", systemInstruction + "\n\n" + prompt)
                        ))
                )
        );

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String rawResponse = restClient.post()
                        .uri(apiUrl + "?key=" + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

                return parseGeminiResponse(rawResponse);
            } catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable ex) {
                log.warn("Gemini service busy (503). Retrying attempt {}/{}...", attempt, maxAttempts);
                if (attempt == maxAttempts) {
                    throw new RuntimeException("Gemini service is temporarily unavailable due to high demand. Please try again shortly.");
                }
                try {
                    Thread.sleep(1500L * attempt); // Wait 1.5s then 3s
                } catch (InterruptedException ignored) {}
            } catch (Exception ex) {
                log.error("Error communicating with Gemini API", ex);
                throw new RuntimeException("Failed to generate AI subtasks: " + ex.getMessage());
            }
        }

        return Collections.emptyList();
    }

    private List<AiSubtaskResponse> parseGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode textNode = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            if (textNode.isMissingNode()) {
                return Collections.emptyList();
            }

            String content = textNode.asText().trim();
            // Clean markdown wrappers if returned
            if (content.startsWith("```json")) {
                content = content.substring(7);
            }
            if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }

            return objectMapper.readValue(content.trim(), new TypeReference<List<AiSubtaskResponse>>() {});
        } catch (Exception e) {
            log.error("Failed to parse Gemini JSON output", e);
            throw new RuntimeException("Error parsing AI subtasks");
        }
    }
}