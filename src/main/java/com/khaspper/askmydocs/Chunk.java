package com.khaspper.askmydocs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    /** The file this slice came from. Deleting the file deletes the slice too. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Document document;

    @Column(columnDefinition = "vector(768)")
    private float[] embeddings;

    @Column(nullable = false)
    private int tries = 0;

    public Chunk(String chunk, int position, Document document) {
        this.chunk = chunk;
        this.position = position;
        this.document = document;
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

    public Document getDocument() {
        return document;
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
