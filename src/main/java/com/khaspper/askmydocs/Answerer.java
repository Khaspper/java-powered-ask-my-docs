package com.khaspper.askmydocs;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Answerer {

    private static final String RULES = """
            Answer the question using only the text between the lines below.
            If the answer is not in that text, say you don't know.
            Do not use anything else you know.
            """;

    private static final double TEMPERATURE = 0.0;

    private final RestClient http;
    private final String apiKey;
    private final String model;

    public Answerer(@Value("${llm.base-url}") String baseUrl,
                    @Value("${llm.api-key}") String apiKey,
                    @Value("${llm.generation-model}") String model) {
        this.http = RestClient.create(baseUrl);
        this.apiKey = apiKey;
        this.model = model;
    }

    private record Message(String content) {
    }

    private record Choice(Message message) {
    }

    private record ChatResponse(List<Choice> choices) {
    }

    public String answer(String question, List<String> texts) {
        StringBuilder prompt = new StringBuilder();
        for (String text : texts) {
            prompt.append("\n---\n").append(text);
        }
        prompt.append("\n---\n\nQuestion: ").append(question);

        ChatResponse response = http.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of(
                        "model", model,
                        "temperature", TEMPERATURE,
                        "messages", List.of(
                                Map.of("role", "system", "content", RULES),
                                Map.of("role", "user", "content", prompt.toString()))))
                .retrieve()
                .body(ChatResponse.class);

        String answer = response.choices().get(0).message().content();
        return answer == null ? "" : answer.strip();
    }
}
