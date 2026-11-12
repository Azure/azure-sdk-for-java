// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class APIListingTests {
    @Test
    public void compressedOutputRoundTripsToRawJson(@TempDir Path directory) throws IOException {
        APIListing listing = new APIListing();
        listing.setLanguage(Language.JAVA);
        listing.setLanguageVariant(LanguageVariant.DEFAULT);
        listing.setPackageName("com.azure:azure-test");
        listing.setPackageVersion("1.0.0");
        Path jsonFile = directory.resolve("api.json");
        Path gzipFile = directory.resolve("api.json.gz");
        byte[] rawJson = listing.toFile(jsonFile.toFile(), false);

        assertArrayEquals(rawJson, listing.toFile(gzipFile.toFile(), true));

        try (GZIPInputStream input = new GZIPInputStream(Files.newInputStream(gzipFile));
            ByteArrayOutputStream decompressed = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                decompressed.write(buffer, 0, count);
            }
            assertArrayEquals(Files.readAllBytes(jsonFile), decompressed.toByteArray());
        }
    }
}
