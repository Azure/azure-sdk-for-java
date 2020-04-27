// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.KnownAuthorityHosts;
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
        String credential;
        if (Platform.isWindows()) {
            WindowsCredentialAccessor winCredAccessor =
                    new WindowsCredentialAccessor(serviceName, accountName);
            credential = winCredAccessor.read();
        } else if (Platform.isMac()) {
            KeyChainAccessor keyChainAccessor = new KeyChainAccessor(null,
                    serviceName, accountName);

            byte[] readCreds = keyChainAccessor.read();
            credential = new String(readCreds, StandardCharsets.UTF_8);
        } else if (Platform.isLinux()) {

            LinuxKeyRingAccessor keyRingAccessor = new LinuxKeyRingAccessor(
                    "org.freedesktop.Secret.Generic", "service",
                    serviceName, "account", accountName);

            byte[] readCreds = keyRingAccessor.read();
            credential = new String(readCreds, StandardCharsets.UTF_8);
        } else {
            throw logger.logExceptionAsError(
                new CredentialUnavailableException("Platform could not be determined for VsCode"
                                                           + " Credential authentication."));
        }
        if (!isRefreshTokenString(credential)) {
            throw logger.logExceptionAsError(
                    new CredentialUnavailableException("Please authenticate via Azure Tools plugin in VSCode IDE."));
        }
        return credential;
    }

    private boolean isRefreshTokenString(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if ((ch < '0' || ch > '9') && (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z')
                        && ch != '_' && ch != '-' && ch != '.') {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the auth host of the specified {@code azureEnvironment}.
     * @param azureEnvironment
     * @return the auth host.
     */
    public String getAzureAuthHost(String azureEnvironment) {

        switch (azureEnvironment) {
            case "Azure":
                return KnownAuthorityHosts.AZURE_CLOUD;
            case "AzureChina":
                return KnownAuthorityHosts.AZURE_CHINA_CLOUD;
            case "AzureGermanCloud":
                return KnownAuthorityHosts.AZURE_GERMAN_CLOUD;
            case "AzureUSGovernment":
                return KnownAuthorityHosts.AZURE_US_GOVERNMENT;
            default:
                return KnownAuthorityHosts.AZURE_CLOUD;
        }
    }

}
