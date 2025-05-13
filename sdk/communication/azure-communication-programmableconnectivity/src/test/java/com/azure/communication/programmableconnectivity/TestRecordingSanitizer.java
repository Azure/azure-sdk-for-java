// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.core.test.TestMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to directly sanitize sensitive information in test recordings.
 */
public class TestRecordingSanitizer {
    private static final Pattern GATEWAY_ID_PATTERN = Pattern.compile("\"apc-gateway-id\": \"([^\"]+)\"");

    /**
     * Sanitizes a test recording file by directly replacing sensitive patterns.
     * 
     * @param recordingFilePath Path to the recording file.
     */
    public static void sanitizeRecording(String recordingFilePath) {
        Path path = Paths.get(recordingFilePath);
        if (!Files.exists(path)) {
            System.out.println("Recording file not found: " + recordingFilePath);
            return;
        }

        try {
            // Read the file content using Java 8 compatible methods
            byte[] encoded = Files.readAllBytes(path);
            String content = new String(encoded, StandardCharsets.UTF_8);

            // Replace the gateway ID header
            Matcher matcher = GATEWAY_ID_PATTERN.matcher(content);
            if (matcher.find()) {
                content = matcher.replaceAll("\"apc-gateway-id\": \"sanitized-gateway-id\"");

                // Write back the sanitized content using Java 8 compatible methods
                Files.write(path, content.getBytes(StandardCharsets.UTF_8));
                System.out.println("Sanitized gateway ID in recording: " + recordingFilePath);
            } else {
                System.out.println("No gateway ID pattern found in: " + recordingFilePath);
            }
        } catch (IOException e) {
            System.err.println("Error sanitizing recording: " + e.getMessage());
        }
    }
}
