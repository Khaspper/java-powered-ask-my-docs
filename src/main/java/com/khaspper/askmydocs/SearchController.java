package com.khaspper.askmydocs;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Ask a question get back the chunks of your files that are closest to it... */
@RestController
public class SearchController {

    private static final int DEFAULT_K = 5;

    private final Embedder embedder;
    private final ChunkRepository chunks;

    public SearchController(Embedder embedder, ChunkRepository chunks) {
        this.embedder = embedder;
        this.chunks = chunks;
    }

    public record SearchRequest(String question, Integer k) {
    }

    public record SearchHit(Long chunkId, Long documentId, String filename, String text,
                            double score) {
    }

    @PostMapping("/search")
    public List<SearchHit> search(@RequestBody SearchRequest request) {
        int k = request.k() == null ? DEFAULT_K : request.k();

        float[] numbers = embedder.embedQuery(request.question());

        return chunks.findClosest(asVectorText(numbers), k).stream()
                .map(m -> new SearchHit(m.getChunkId(), m.getDocumentId(), m.getFilename(),
                        m.getText(), m.getScore()))
                .toList();
    }

    private static String asVectorText(float[] numbers) {
        StringBuilder text = new StringBuilder("[");
        for (int i = 0; i < numbers.length; i++) {
            if (i > 0) {
                text.append(',');
            }
            text.append(numbers[i]);
        }
        return text.append(']').toString();
    }
}
