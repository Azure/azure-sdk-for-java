// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4jextensions.persistence.mac.KeyChainAccessor;
import com.sun.jna.Platform;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * This class allows access to Visual Studio Code cached credential data.
 */
public class VisualStudioCacheAccessor {
    private final ClientLogger logger = new ClientLogger(VisualStudioCacheAccessor.class);

    /**
     * Creates an instance of {@link VisualStudioCacheAccessor}
     */
    public VisualStudioCacheAccessor() { }

    /**
     * Get the user configured settings of Visual Studio code.
     * @return the {@link JsonNode} holding the settings as properties.
     */
    public JsonNode getUserSettings() {
        JsonNode output = null;
        String homeDir = System.getProperty("user.home");
        String settingsPath = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (Platform.isWindows()) {
                settingsPath = Paths.get(System.getenv("APPDATA"), "Code", "User", "settings.json")
                        .toString();
            } else if (Platform.isMac()) {
                settingsPath = Paths.get(homeDir, "Library",
                        "Application Support", "Code", "User", "settings.json").toString();
            } else if (Platform.isLinux()) {
                settingsPath = Paths.get(homeDir, ".config", "Code", "User", "settings.json")
                        .toString();
            }
            File settingsFile = new File(settingsPath);
            output = mapper.readTree(settingsFile);
        } catch (Exception e) {
            return output;
        }
        return output;
    }

    /**
     * Get the credential for the specified service and account name.
     *
     * @param serviceName the name of the service to lookup.
     * @param accountName the account of the service to lookup.
     * @return the credential.
     */
    public String getCredentials(String serviceName, String accountName) {
        if (Platform.isWindows()) {
            WindowsCredentialAccessor winCredAccessor =
                    new WindowsCredentialAccessor(serviceName, accountName);
            return winCredAccessor.read();
        } else if (Platform.isMac()) {
            KeyChainAccessor keyChainAccessor = new KeyChainAccessor(null,
                    serviceName, accountName);

            byte[] readCreds = keyChainAccessor.read();
            return new String(readCreds, StandardCharsets.UTF_8);
        } else if (Platform.isLinux()) {

            LinuxKeyRingAccessor keyRingAccessor = new LinuxKeyRingAccessor(
                    "org.freedesktop.Secret.Generic", "service",
                    serviceName, "account", accountName);

            byte[] readCreds = keyRingAccessor.read();
            return new String(readCreds, StandardCharsets.UTF_8);
        } else {
            throw logger.logExceptionAsError(
                new RuntimeException("Platform could not be determined for VsCode Credential authentication."));
        }
    }

}
