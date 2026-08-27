package com.khaspper.askmydocs;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<Chunk, Long>{

    /** How many chunks belong to one document. */
    long countByDocument(Document document);
}
