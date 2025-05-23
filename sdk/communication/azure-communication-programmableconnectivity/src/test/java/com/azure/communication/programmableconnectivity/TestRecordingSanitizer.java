// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to directly sanitize sensitive information in test recordings.
 */
public class TestRecordingSanitizer {

    public static boolean sanitizeRecording(String recordingFilePath) {
        Path path = Paths.get(recordingFilePath);

        // If not found, try relative paths based on project structure
        if (!Files.exists(path)) {
            System.out.println("Recording file not found at: " + recordingFilePath);

            Path currentDir = Paths.get(System.getProperty("user.dir"));

            Path assetsPath = currentDir.resolve(
                "../../../.assets/tcGqIyvsbC/java/sdk/communication/azure-communication-programmableconnectivity/src/test/resources/session-records")
                .normalize();

            Path assetFilePath = assetsPath.resolve(new File(recordingFilePath).getName());
            if (Files.exists(assetFilePath)) {
                path = assetFilePath;
                System.out.println("Found recording file at: " + path);
            } else {
                System.out.println("Recording file not found at: " + assetFilePath);
                return false;
            }
        }

        try {
            // Read the entire file as a string
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            // Find and print the gateway ID before sanitization 
            int startIndex = content.indexOf("apc-gateway-id");
            if (startIndex != -1) {
                System.out.println("Found apc-gateway-id in content");

                int valueStart = content.indexOf(':', startIndex) + 1;
                while (valueStart < content.length()
                    && (content.charAt(valueStart) == ' ' || content.charAt(valueStart) == '"')) {
                    valueStart++;
                }

                int valueEnd = content.indexOf('"', valueStart);
                if (valueEnd != -1) {
                    String gatewayId = content.substring(valueStart, valueEnd);
                    System.out.println("Current gateway ID: " + gatewayId);

                    // Reconstruct the line with sanitized gateway ID
                    String beforeValue = content.substring(0, valueStart);
                    String afterValue = content.substring(valueEnd);

                    // Create sanitized content
                    String sanitizedContent = beforeValue + "sanitized-gateway-id" + afterValue;

                    // Verify the sanitization worked
                    if (sanitizedContent.contains("sanitized-gateway-id")) {
                        System.out.println("Sanitization successful in memory");

                        // Write the sanitized content back to the file using atomic write
                        Path tempFile = Files.createTempFile("sanitized", ".json");
                        Files.write(tempFile, sanitizedContent.getBytes(StandardCharsets.UTF_8));
                        Files.move(tempFile, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                        // Verify the file was updated correctly
                        String verifyContent = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                        if (verifyContent.contains("sanitized-gateway-id")) {
                            System.out.println("Successfully sanitized gateway ID in file");
                            return true;
                        } else {
                            System.out.println("WARNING: File does not contain sanitized-gateway-id after write!");
                            return false;
                        }
                    } else {
                        System.out.println("Sanitization failed in memory");
                        return false;
                    }
                }
            }

            System.out.println("Gateway ID not found in content");
            return false;

        } catch (IOException e) {
            System.err.println("Error sanitizing recording: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
