// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apireviewplugin;

import com.azure.tools.apiview.processor.model.ApiListing;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReviewBundleWriterTests {
    @TempDir
    Path directory;

    @Test
    public void restoresBothFilesIfPublishingTheSecondFileFails() throws Exception {
        Files.write(directory.resolve("api.md"), "old API".getBytes(StandardCharsets.UTF_8));
        Files.write(directory.resolve("api.metadata.yml"), "old metadata".getBytes(StandardCharsets.UTF_8));
        ReviewBundleWriter writer = new ReviewBundleWriter((source, destination) -> {
            if (destination.getFileName().toString().equals("api.metadata.yml")) {
                throw new IOException("Simulated metadata replacement failure.");
            }
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        });
        assertThrows(IOException.class, () -> writer.write(new ApiListing(), "1.0.0", "sdk/test", directory));
        assertEquals("old API", new String(Files.readAllBytes(directory.resolve("api.md")), StandardCharsets.UTF_8));
        assertEquals("old metadata",
            new String(Files.readAllBytes(directory.resolve("api.metadata.yml")), StandardCharsets.UTF_8));
        try (Stream<Path> paths = Files.list(directory)) {
            assertEquals(2, paths.count());
        }
    }
}
