package com.khaspper.askmydocs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Answerer {

    private static final String BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final String RULES = """
            Answer the question using only the text between the lines below.
            If the answer is not in that text, say you don't know.
            Do not use anything else you know.
            """;

    private final RestClient http = RestClient.create();
    private final String apiKey;
    private final String model;

    public Answerer(@Value("${gemini.api-key}") String apiKey,
                    @Value("${gemini.generation-model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    private record Part(String text) {
    }

    private record Content(List<Part> parts) {
    }

    private record Candidate(Content content) {
    }

    private record GenerateResponse(List<Candidate> candidates) {
    }

    public String answer(String question, List<String> texts) {
        StringBuilder prompt = new StringBuilder(RULES);
        for (String text : texts) {
            prompt.append("\n---\n").append(text);
        }
        prompt.append("\n---\n\nQuestion: ").append(question);

        GenerateResponse response = http.post()
                .uri(BASE + model + ":generateContent")
                .header("x-goog-api-key", apiKey)
                .body(Map.of("contents",
                        List.of(Map.of("parts", List.of(Map.of("text", prompt.toString()))))))
                .retrieve()
                .body(GenerateResponse.class);

        return response.candidates().get(0).content().parts().stream()
                .map(Part::text)
                .filter(text -> text != null)
                .collect(Collectors.joining());
    }
}
