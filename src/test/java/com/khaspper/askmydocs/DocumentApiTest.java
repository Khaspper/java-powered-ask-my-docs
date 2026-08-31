package com.khaspper.askmydocs;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class DocumentApiTest extends IntegrationTestBase {

    private static MockMultipartFile textFile(String name, String text) {
        return new MockMultipartFile("file", name, MediaType.TEXT_PLAIN_VALUE, text.getBytes());
    }

    @Test
    void uploadingATextFileIsAccepted() throws Exception {
        http.perform(multipart("/documents").file(textFile("notes.txt", "a".repeat(2500))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("notes.txt"))
                .andExpect(jsonPath("$.chunks").value(3))
                .andExpect(jsonPath("$.embedded").value(3));
    }

    @Test
    void theSameFileTwiceIsRefused() throws Exception {
        MockMultipartFile sameBytes = textFile("twice.txt", "the very same bytes both times");

        http.perform(multipart("/documents").file(sameBytes))
                .andExpect(status().isCreated());

        http.perform(multipart("/documents").file(sameBytes))
                .andExpect(status().isConflict());
    }

    @Test
    void aPictureIsRefused() throws Exception {
        MockMultipartFile picture =
                new MockMultipartFile("file", "cat.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

        http.perform(multipart("/documents").file(picture))
                .andExpect(status().isBadRequest());
    }

    @Test
    void theListShowsWhatWasUploaded() throws Exception {
        http.perform(multipart("/documents").file(textFile("inthelist.txt", "only this test uploads this")))
                .andExpect(status().isCreated());

        http.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].filename").value(hasItem("inthelist.txt")));
    }
}
