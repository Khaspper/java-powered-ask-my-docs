package com.khaspper.askmydocs;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChunkRepository extends JpaRepository<Chunk, Long>{

    /** How many chunks belong to one document. */
    long countByDocument(Document document);

    interface Match {
        Long getChunkId();

        Long getDocumentId();

        String getFilename();

        String getText();

        double getScore();
    }

    /**
     * The k closest slices to a question
     *
     * This one is written by hand because no method name can say `<=>`
     */
    @Query(value = """
            select c.id       as "chunkId",
                   d.id       as "documentId",
                   d.filename as "filename",
                   c.chunk    as "text",
                   1 - (c.embeddings <=> cast(:question as vector)) as "score"
            from chunks c
            join documents d on d.id = c.document_id
            where c.embeddings is not null
            order by c.embeddings <=> cast(:question as vector)
            limit :k
            """, nativeQuery = true)
    List<Match> findClosest(@Param("question") String question, @Param("k") int k);
}
