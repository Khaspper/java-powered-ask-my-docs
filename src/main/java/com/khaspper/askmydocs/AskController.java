package com.khaspper.askmydocs;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskController {

    private static final String NO_ANSWER = "I don't know.";

    private final SearchController search;
    private final Answerer answerer;

    public AskController(SearchController search, Answerer answerer) {
        this.search = search;
        this.answerer = answerer;
    }

    public record Source(Long chunkId, String filename, double score) {
    }

    public record AskResponse(String answer, List<Source> sources) {
    }

    @PostMapping("/ask")
    public AskResponse ask(@RequestBody SearchController.SearchRequest request) {
        List<SearchController.SearchHit> hits = search.search(request);

        if (hits.isEmpty()) {
            return new AskResponse(NO_ANSWER, List.of());
        }

        String answer = answerer.answer(request.question(),
                hits.stream().map(SearchController.SearchHit::text).toList());

        return new AskResponse(answer, hits.stream()
                .map(hit -> new Source(hit.chunkId(), hit.filename(), hit.score()))
                .toList());
    }
}
