// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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

    @Test
    void getAccessTokenDoesNotLogTheClientSecret() {
        String clientSecret = "the-client-secret-value";
        List<String> loggedValues = new ArrayList<>();
        Handler collector = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                if (logRecord.getParameters() != null) {
                    for (Object parameter : logRecord.getParameters()) {
                        loggedValues.add(String.valueOf(parameter));
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        Logger logger = Logger.getLogger(AccessTokenUtil.class.getName());
        Level originalLevel = logger.getLevel();
        boolean originalUseParentHandlers = logger.getUseParentHandlers();

        logger.addHandler(collector);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        try {
            AccessTokenUtil.getAccessToken("https://vault.azure.net", null, "tenant-id", "client-id", clientSecret,
                (uri, headers, body, contentType) -> null);
        } finally {
            logger.removeHandler(collector);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(originalUseParentHandlers);
        }

        assertFalse(loggedValues.contains(clientSecret), "The client secret must never be logged");
        assertTrue(loggedValues.contains("client-id"), "Non-secret parameters stay available for diagnostics");
    }
}
