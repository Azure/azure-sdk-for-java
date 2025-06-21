// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker.implementation;

import com.azure.identity.AuthenticationRecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class BrokerUtil {
    public static final Path VSCODE_AUTH_RECORD_PATH = Paths.get(System.getProperty("user.home"), ".azure",
        "ms-azuretools.vscode-azureresourcegroups", "authRecord.json");

    public static boolean isVsCodeBrokerAuthAvailable() {
        try {
            // 1. Check if Broker dependency is available
            Class.forName("com.azure.identity.broker.InteractiveBrowserBrokerCredentialBuilder");

            // 2. Check if VS Code broker auth record file exists
            File authRecordFile = VSCODE_AUTH_RECORD_PATH.toFile();

            return authRecordFile.exists() && authRecordFile.isFile();
        } catch (ClassNotFoundException e) {
            return false; // Broker not present
        }
    }

    /**
     * Loads the VS Code AuthenticationRecord if present on disk.
     *
     * @return The deserialized AuthenticationRecord, or {@code null} if the file doesn't exist.
     * @throws IOException if reading the file fails
     */
    public static AuthenticationRecord loadVSCodeAuthRecord() throws IOException {
        if (!Files.exists(VSCODE_AUTH_RECORD_PATH) || !Files.isRegularFile(VSCODE_AUTH_RECORD_PATH)) {
            return null;
        }

        try (InputStream jsonStream = Files.newInputStream(VSCODE_AUTH_RECORD_PATH)) {
            return AuthenticationRecord.deserialize(jsonStream);
        }
    }
}
