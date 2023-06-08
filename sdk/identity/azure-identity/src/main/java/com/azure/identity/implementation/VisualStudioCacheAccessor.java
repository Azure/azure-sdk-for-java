// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.CredentialUnavailableException;
import com.azure.json.JsonOptions;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.microsoft.aad.msal4jextensions.persistence.mac.KeyChainAccessor;
import com.sun.jna.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * This class allows access to Visual Studio Code cached credential data.
 */
public class VisualStudioCacheAccessor {
    private static final String PLATFORM_NOT_SUPPORTED_ERROR = "Platform could not be determined for VS Code"
        + " credential authentication.";
    private static final Pattern REFRESH_TOKEN_PATTERN = Pattern.compile("^[-_.a-zA-Z0-9]+$");
    private static final JsonOptions LENIENT_OPTIONS = new JsonOptions()
        .setAllowComments(true)
        .setAllowTrailingCommas(true)
        .setAllowUnescapedControlCharacters(true);

    private Map<String, Object> getUserSettings() {
        Map<String, Object> output;
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
                throw new CredentialUnavailableException(PLATFORM_NOT_SUPPORTED_ERROR);
            }
            output = readJsonFile(settingsPath);
        } catch (Exception e) {
            return null;
        }
        return output;
    }

    static Map<String, Object> readJsonFile(String path) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(Files.newBufferedReader(new File(path).toPath(),
            StandardCharsets.UTF_8), LENIENT_OPTIONS)) {
            return jsonReader.readMap(JsonReader::readUntyped);
        }
    }

    /**
     * Get the user configured settings of Visual Studio code.
     *
     * @return a Map containing VS Code user settings
     */
    public Map<String, String> getUserSettingsDetails() {
        Map<String, Object> userSettings = getUserSettings();
        Map<String, String> details = new HashMap<>();

        String tenant = null;
        String cloud = "AzureCloud";

        if (userSettings != null) {
            tenant = Objects.toString(userSettings.get("azure.tenant"), null);
            cloud = Objects.toString(userSettings.get("azure.cloud"), null);
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
                throw new CredentialUnavailableException("Failed to read Vs Code credentials from"
                    + " Windows Credential API.", e);
            }

        } else if (Platform.isMac()) {

            try {
                KeyChainAccessor keyChainAccessor = new KeyChainAccessor(null, serviceName, accountName);

                byte[] readCreds = keyChainAccessor.read();
                credential = new String(readCreds, StandardCharsets.UTF_8);
            } catch (Exception | Error e) {
                throw new CredentialUnavailableException("Failed to read Vs Code credentials"
                    + " from Mac Native Key Chain.", e);
            }

        } else if (Platform.isLinux()) {

            try {
                LinuxKeyRingAccessor keyRingAccessor = new LinuxKeyRingAccessor(
                    "org.freedesktop.Secret.Generic", "service",
                    serviceName, "account", accountName);

                byte[] readCreds = keyRingAccessor.read();
                credential = new String(readCreds, StandardCharsets.UTF_8);
            } catch (Exception | Error e) {
                throw new CredentialUnavailableException("Failed to read Vs Code credentials from Linux Key Ring.", e);
            }

        } else {
            throw new CredentialUnavailableException(PLATFORM_NOT_SUPPORTED_ERROR);
        }

        if (CoreUtils.isNullOrEmpty(credential) || !isRefreshTokenString(credential)) {
            throw new CredentialUnavailableException("Please authenticate via Azure Tools plugin in VS Code IDE.");
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
