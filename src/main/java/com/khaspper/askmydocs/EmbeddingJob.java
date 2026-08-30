package com.khaspper.askmydocs;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Crom run every 60 seconds
@Component
public class EmbeddingJob {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingJob.class);

    private static final int BATCH = 50;
    private static final int MAX_TRIES = 3;

    private final ChunkRepository chunks;
    private final Embedder embedder;

    public EmbeddingJob(ChunkRepository chunks, Embedder embedder) {
        this.chunks = chunks;
        this.embedder = embedder;
    }

    /** 60_000 milliseconds after the last run finished, run again. */
    @Scheduled(fixedDelay = 60_000)
    public void fillInMissingNumbers() {
        List<Chunk> waiting =
                chunks.findByEmbeddingsIsNullAndTriesLessThan(MAX_TRIES, Limit.of(BATCH));

        if (waiting.isEmpty()) {
            return;
        }

        log.info("Catch-up job: {} slice(s) with no numbers", waiting.size());
        int done = 0;
        for (Chunk chunk : waiting) {
            try {
                chunk.setEmbeddings(embedder.embedDocument(chunk.getChunk()));
                done += 1;
            } catch (Exception e) {
                chunk.increaseTries();
                log.warn("Slice {} failed again, now {} tries: {}",
                        chunk.getId(), chunk.getTries(), e.toString());
            }
        }
        chunks.saveAll(waiting);
        log.info("Catch-up job: filled in {} of {}", done, waiting.size());
    }
}
