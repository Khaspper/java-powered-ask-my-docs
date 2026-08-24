package com.khaspper.askmydocs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One uploaded file.
 * This class is a shape... One object of it = one row in the "documents" table
 * The labels below tell Hibernate how to map each field to a column
 */
@Entity                     // "this class is a table"
@Table(name = "documents")  // ...called documents
public class Document {

    @Id                                                  // this field is the row's identity
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Postgres hands out the number
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String contentType;

    /** SHA-256 of the file's bytes. Unique, so the same file can't land twice. */
    @Column(nullable = false, unique = true, length = 64)
    private String sha256;

    /** The whole extracted text. columnDefinition = "text" avoids Postgres' 255 default. */
    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(nullable = false)
    private Instant uploadedAt;

    /** Hibernate needs an empty constructor to build objects when reading rows. */
    protected Document() {
    }

    public Document(String filename, String contentType, String sha256, String text) {
        this.filename = filename;
        this.contentType = contentType;
        this.sha256 = sha256;
        this.text = text;
        this.uploadedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public String getSha256() {
        return sha256;
    }

    public String getText() {
        return text;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}
