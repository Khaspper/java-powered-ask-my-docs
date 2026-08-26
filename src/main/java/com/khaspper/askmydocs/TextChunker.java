package com.khaspper.askmydocs;

import java.util.ArrayList;
import java.util.List;

public final class TextChunker {

    /** How many characters in one slice */
    public static final int CHUNK_SIZE = 1000;

    /** How many characters each slice repeats from the one before it */
    public static final int OVERLAP = 200;

    /** So each new slice starts 800 characters after the last one started */
    private static final int STEP = CHUNK_SIZE - OVERLAP;

    /** Nobody should ever build one of these. Everything here is static. */
    private TextChunker() {
    }

    public static List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();

        // Nothing in, nothing out. Also stops the loop below from running.
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            // Don't read past the end of the text.
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));

            // Reached the end, so the slice just added was the last one.
            if (end == text.length()) {
                break;
            }
            start += STEP;
        }

        return chunks;
    }
}
