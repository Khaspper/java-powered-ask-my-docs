package com.khaspper.askmydocs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/** Turns a file's raw bytes into plain text */
@Component  // "Spring, make one of these and keep it for whoever needs it"
public class TextExtractor {

    public String extract(byte[] bytes, String extension) throws IOException {
        if (extension.equals("pdf")) {
            // PDFBox opens the bytes as a PDF and walks the pages for words
            try (var pdf = Loader.loadPDF(bytes)) {
                return new PDFTextStripper().getText(pdf);
            }
        }
        // txt and md are already text... Just read the bytes as characters
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
