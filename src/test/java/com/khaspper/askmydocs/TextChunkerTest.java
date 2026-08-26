package com.khaspper.askmydocs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Checks the cutting rules from the spec. No database, no app start.
 */
class TextChunkerTest {

    /** Builds a string of the given length, so lengths are easy to check. */
    private static String textOfLength(int length) {
        return "a".repeat(length);
    }

    @Test
    void twoThousandFiveHundredCharactersGivesThreeChunks() {
        List<String> chunks = TextChunker.chunk(textOfLength(2500));

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0)).hasSize(1000);
        assertThat(chunks.get(1)).hasSize(1000);
        assertThat(chunks.get(2)).hasSize(900);  // 1600 to 2500
    }

    @Test
    void emptyTextGivesNoChunks() {
        assertThat(TextChunker.chunk("")).isEmpty();
        assertThat(TextChunker.chunk(null)).isEmpty();
    }

    @Test
    void shortTextGivesOneChunkThatIsNotPadded() {
        List<String> chunks = TextChunker.chunk(textOfLength(50));

        assertThat(chunks).containsExactly(textOfLength(50));
    }

    @Test
    void eachChunkRepeatsTheLastTwoHundredCharactersOfTheOneBefore() {
        // Numbered so a repeat is visible, not just a wall of the same letter.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; sb.length() < 2500; i++) {
            sb.append(i % 10);
        }
        List<String> chunks = TextChunker.chunk(sb.toString());

        String tailOfFirst = chunks.get(0).substring(800);
        String headOfSecond = chunks.get(1).substring(0, 200);

        assertThat(tailOfFirst).isEqualTo(headOfSecond);
        assertThat(tailOfFirst).hasSize(200);
    }
}
