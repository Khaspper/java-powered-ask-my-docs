package com.khaspper.askmydocs;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DocumentController {

    private static final List<String> ALLOWED = List.of("txt", "md", "pdf");
    private static final long MAX_BYTES = 10L * 1024 * 1024;  // 10 MB

    private final DocumentRepository documents;
    private final TextExtractor textExtractor;

    public DocumentController(DocumentRepository documents, TextExtractor textExtractor) {
        this.documents = documents;
        this.textExtractor = textExtractor;
    }

    public record UploadResponse(Long id, String filename) {
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public UploadResponse upload(@RequestParam("file") MultipartFile file) throws IOException {

        String filename = file.getOriginalFilename();
        String extension = extensionOf(filename);

        if (!ALLOWED.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only txt, md and pdf are allowed. Got: " + extension);
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Files must be 10 MB or smaller.");
        }

        byte[] bytes = file.getBytes();
        String sha256 = sha256Of(bytes);

        // Same bytes as something already stored?
        var existing = documents.findBySha256(sha256);
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Already uploaded as document " + existing.get().getId());
        }

        String text = textExtractor.extract(bytes, extension);
        Document saved = documents.save(new Document(filename, extension, sha256, text));
        return new UploadResponse(saved.getId(), saved.getFilename());
    }

    // One row of the list
    public record DocumentSummary(Long id, String filename, Instant uploadedAt) {
    }

    @GetMapping("/documents")
    public List<DocumentSummary> list() {
        return documents.findAll().stream()
                .map(d -> new DocumentSummary(d.getId(), d.getFilename(), d.getUploadedAt()))
                .toList();
    }

    // grab extention
    private static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }

    // File's fingerprint
    private static String sha256Of(byte[] bytes) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) {
                hex.append("%02x".formatted(b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // Every Java install has SHA-256. This can't actually happen.
            throw new IllegalStateException(e);
        }
    }
}
