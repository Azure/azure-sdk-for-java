// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccessTokenUtil utility methods.
 */
public class AccessTokenUtilTest {

    @Test
    void testReadFile(@TempDir Path tempDir) throws Exception {
        Path tempFile = Files.createTempFile(tempDir, "simple_text_file_", ".txt");
        String expectedContent = "Just a dummy string";
        Files.write(tempFile, expectedContent.getBytes(StandardCharsets.UTF_8));

        String actualContent = AccessTokenUtil.readFile(tempFile.toAbsolutePath().toString());
        assertNotNull(actualContent);
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void testReadFileWithNonExistentFile() {
        String actualContent = AccessTokenUtil.readFile("/non/existent/file.txt");
        assertNull(actualContent);
    }
}
