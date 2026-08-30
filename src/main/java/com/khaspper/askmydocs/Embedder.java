package com.khaspper.askmydocs;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Embedder {

    private static final String DOCUMENT_PREFIX = "search_document: ";
    private static final String QUERY_PREFIX = "search_query: ";

    private final RestClient http;
    private final String apiKey;
    private final String model;

    public Embedder(@Value("${llm.base-url}") String baseUrl,
                    @Value("${llm.api-key}") String apiKey,
                    @Value("${llm.embedding-model}") String model) {
        this.http = RestClient.create(baseUrl);
        this.apiKey = apiKey;
        this.model = model;
    }

    private record Item(List<Double> embedding) {
    }

    private record EmbedResponse(List<Item> data) {
    }

    public float[] embedDocument(String text) {
        return embed(DOCUMENT_PREFIX + text);
    }

    public float[] embedQuery(String text) {
        return embed(QUERY_PREFIX + text);
    }

    private float[] embed(String text) {
        EmbedResponse response = http.post()
                .uri("/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of("model", model, "input", text))
                .retrieve()
                .body(EmbedResponse.class);

        List<Double> values = response.data().get(0).embedding();
        float[] numbers = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            numbers[i] = values.get(i).floatValue();
        }
        return numbers;
    }
}
