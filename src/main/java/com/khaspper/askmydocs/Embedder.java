package com.khaspper.askmydocs;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Embedder {

    private static final String BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final int DIMENSIONS = 768;

    private final RestClient http = RestClient.create();
    private final String apiKey;
    private final String model;

    public Embedder(@Value("${gemini.api-key}") String apiKey,
                    @Value("${gemini.embedding-model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    private record Embedding(List<Double> values) {
    }

    private record EmbedResponse(Embedding embedding) {
    }

    public float[] embed(String text) {
        EmbedResponse response = http.post()
                .uri(BASE + model + ":embedContent")
                .header("x-goog-api-key", apiKey)
                .body(Map.of(
                        "content", Map.of("parts", List.of(Map.of("text", text))),
                        "outputDimensionality", DIMENSIONS))
                .retrieve()
                .body(EmbedResponse.class);

        List<Double> values = response.embedding().values();
        float[] numbers = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            numbers[i] = values.get(i).floatValue();
        }
        return numbers;
    }
}
