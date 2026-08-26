package com.khaspper.askmydocs;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The way to read and write documents.
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findBySha256(String sha256);
}
