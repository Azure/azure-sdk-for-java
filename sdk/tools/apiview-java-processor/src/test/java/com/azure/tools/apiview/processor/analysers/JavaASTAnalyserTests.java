package com.azure.tools.apiview.processor.analysers;

import com.azure.tools.apiview.processor.model.APIListing;
import com.azure.tools.apiview.processor.model.MarkdownRenderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaASTAnalyserTests {
    @Test
    public void rendersServiceVersionReturnHintAfterMethodSignature(@TempDir Path tempDir) throws IOException {
        Path sourceFile = tempDir.resolve("TestServiceVersion.java");
        Files.write(sourceFile, String.join(System.lineSeparator(),
            "package com.azure.test;",
            "public enum TestServiceVersion implements ServiceVersion {",
            "    V1;",
            "    public static TestServiceVersion getLatest() {",
            "        return V1;",
            "    }",
            "}").getBytes(StandardCharsets.UTF_8));

        APIListing listing = new APIListing();
        listing.setPackageName("io.clientcore:test");
        new JavaASTAnalyser(listing).analyse(Collections.singletonList(sourceFile));
        String markdown = MarkdownRenderer.render(listing);

        assertTrue(markdown.contains("public static TestServiceVersion getLatest() // returns V1"));
        assertFalse(markdown.contains("getLatest(// returns V1"));
    }
}
