package com.smartorder.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ClaudeApiClient {

    private static final Logger log =
            LoggerFactory.getLogger(ClaudeApiClient.class);

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.url}")
    private String apiUrl;

    @Value("${anthropic.model}")
    private String model;

    private final WebClient webClient =
            WebClient.builder().build();

    public String call(String prompt) {
        log.info("Calling Claude API...");
        try {
            String requestBody = """
                {
                    "model": "%s",
                    "max_tokens": 1000,
                    "messages": [
                        {
                            "role": "user",
                            "content": "%s"
                        }
                    ]
                }
                """.formatted(model,
                    prompt.replace("\"", "'")
                            .replace("\n", " "));

            String response = webClient.post()
                    .uri(apiUrl)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header(HttpHeaders.CONTENT_TYPE,
                            MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Extract text from response
            String result = extractText(response);
            log.info("Claude API response received");
            return result;

        } catch (Exception e) {
            log.error("Claude API error: {}", e.getMessage());
            return "AI analysis unavailable: " + e.getMessage();
        }
    }

    private String extractText(String response) {
        try {
            // Find "text": " and extract until the closing
            int start = response.indexOf("\"text\":\"") + 8;
            if (start < 8) return response;

            StringBuilder result = new StringBuilder();
            int i = start;
            while (i < response.length()) {
                char c = response.charAt(i);
                // Check for end of string
                if (c == '"' && response.charAt(i - 1) != '\\') {
                    break;
                }
                // Handle escape sequences
                if (c == '\\' && i + 1 < response.length()) {
                    char next = response.charAt(i + 1);
                    if (next == 'n') {
                        result.append('\n');
                        i += 2;
                        continue;
                    } else if (next == '"') {
                        result.append('"');
                        i += 2;
                        continue;
                    } else if (next == '\\') {
                        result.append('\\');
                        i += 2;
                        continue;
                    }
                }
                result.append(c);
                i++;
            }
            return result.toString();
        } catch (Exception e) {
            log.error("Error extracting text: {}", e.getMessage());
            return response;
        }
    }
}