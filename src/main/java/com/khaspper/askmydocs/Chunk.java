package com.khaspper.askmydocs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chunks")
public class Chunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String chunk;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private Long documentId;

    @Column(columnDefinition = "vector(768)")
    private float[] embeddings;

    @Column(nullable = false)
    private int tries = 0;

    public Chunk(String chunk, int position, Long documentId) {
        this.chunk = chunk;
        this.position = position;
        this.documentId = documentId;
    }

    /** Hibernate needs an empty constructor to build objects when reading rows. */
    protected Chunk() {
    }

    public int getTries() {
        return tries;
    }

    public float[] getEmbeddings() {
        return embeddings;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public int getPosition() {
        return position;
    }

    public String getChunk() {
        return chunk;
    }

    public Long getId() {
        return id;
    }

    public void setEmbeddings(float[] embedding) {
        this.embeddings = embedding;
    }

    public void increaseTries() {
        this.tries += 1;
    }
}
