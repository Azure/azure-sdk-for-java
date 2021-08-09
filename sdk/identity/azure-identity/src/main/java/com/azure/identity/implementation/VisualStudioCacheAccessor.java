// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.CredentialUnavailableException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4jextensions.persistence.mac.KeyChainAccessor;
import com.sun.jna.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class allows access to Visual Studio Code cached credential data.
 */
public class VisualStudioCacheAccessor {
    private static final String PLATFORM_NOT_SUPPORTED_ERROR = "Platform could not be determined for VS Code"
        + " credential authentication.";
    private final ClientLogger logger = new ClientLogger(VisualStudioCacheAccessor.class);
    private static final Pattern REFRESH_TOKEN_PATTERN = Pattern.compile("^[-_.a-zA-Z0-9]+$");

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true)
        .configure(JsonReadFeature.ALLOW_JAVA_COMMENTS.mappedFeature(), true)
        .configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true);

    private JsonNode getUserSettings() {
        JsonNode output;
        String homeDir = System.getProperty("user.home");
        String settingsPath;
        try {
            if (Platform.isWindows()) {
                settingsPath = Paths.get(System.getenv("APPDATA"), "Code", "User", "settings.json").toString();
            } else if (Platform.isMac()) {
                settingsPath = Paths.get(homeDir, "Library", "Application Support", "Code", "User", "settings.json")
                    .toString();
            } else if (Platform.isLinux()) {
                settingsPath = Paths.get(homeDir, ".config", "Code", "User", "settings.json").toString();
            } else {
                throw logger.logExceptionAsError(new CredentialUnavailableException(PLATFORM_NOT_SUPPORTED_ERROR));
            }
            output = readJsonFile(settingsPath);
        } catch (Exception e) {
            return null;
        }
        return output;
    }

    static JsonNode readJsonFile(String path) throws IOException {
        return MAPPER.readTree(new File(path));
    }

    /**
     * Get the user configured settings of Visual Studio code.
     *
     * @return a Map containing Vs Code user settings
     */
    public Map<String, String> getUserSettingsDetails() {
        JsonNode userSettings = getUserSettings();
        Map<String, String> details = new HashMap<>();

        String tenant = null;
        String cloud = "AzureCloud";

        if (userSettings != null && !userSettings.isNull()) {
            if (userSettings.has("azure.tenant")) {
                tenant = userSettings.get("azure.tenant").asText();
            }

            if (userSettings.has("azure.cloud")) {
                cloud = userSettings.get("azure.cloud").asText();
            }
        }

        if (!CoreUtils.isNullOrEmpty(tenant)) {
            details.put("tenant", tenant);
        }

        details.put("cloud", cloud);
        return details;
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

            try {
                credential = new WindowsCredentialAccessor(serviceName, accountName).read();
            } catch (Exception | Error e) {
                throw logger.logExceptionAsError(new CredentialUnavailableException(
                    "Failed to read Vs Code credentials from Windows Credential API.", e));
            }

        } else if (Platform.isMac()) {

            try {
                KeyChainAccessor keyChainAccessor = new KeyChainAccessor(null, serviceName, accountName);

                byte[] readCreds = keyChainAccessor.read();
                credential = new String(readCreds, StandardCharsets.UTF_8);
            } catch (Exception | Error e) {
                throw logger.logExceptionAsError(new CredentialUnavailableException(
                    "Failed to read Vs Code credentials from Mac Native Key Chain.", e));
            }

        } else if (Platform.isLinux()) {

            try {
                LinuxKeyRingAccessor keyRingAccessor = new LinuxKeyRingAccessor(
                    "org.freedesktop.Secret.Generic", "service",
                    serviceName, "account", accountName);

                byte[] readCreds = keyRingAccessor.read();
                credential = new String(readCreds, StandardCharsets.UTF_8);
            } catch (Exception | Error e) {
                throw logger.logExceptionAsError(new CredentialUnavailableException(
                    "Failed to read Vs Code credentials from Linux Key Ring.", e));
            }

        } else {
            throw logger.logExceptionAsError(
                new CredentialUnavailableException(PLATFORM_NOT_SUPPORTED_ERROR));
        }

        if (CoreUtils.isNullOrEmpty(credential) || !isRefreshTokenString(credential)) {
            throw logger.logExceptionAsError(
                new CredentialUnavailableException("Please authenticate via Azure Tools plugin in VS Code IDE."));
        }
        return credential;
    }

    private boolean isRefreshTokenString(String str) {
        return REFRESH_TOKEN_PATTERN.matcher(str).matches();
    }

    /**
     * Get the auth host of the specified {@code azureEnvironment}.
     *
     * @return the auth host.
     */
    public String getAzureAuthHost(String cloud) {

        switch (cloud) {
            case "AzureCloud":
                return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
            case "AzureChina":
                return AzureAuthorityHosts.AZURE_CHINA;
            case "AzureGermanCloud":
                return AzureAuthorityHosts.AZURE_GERMANY;
            case "AzureUSGovernment":
                return AzureAuthorityHosts.AZURE_GOVERNMENT;
            default:
                return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        }
    }

}
