// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.CredentialUnavailableException;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.microsoft.aad.msal4jextensions.persistence.mac.KeyChainAccessor;
import com.sun.jna.Platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
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
    private static final Pattern REFRESH_TOKEN_PATTERN = Pattern.compile("^[-_.a-zA-Z0-9]+$");


    public static String getSettingsPath() {
        String homeDir = System.getProperty("user.home");
        try {
            if (Platform.isWindows()) {
                return Paths.get(System.getenv("APPDATA"), "Code", "User", "settings.json").toString();
            } else if (Platform.isMac()) {
                return Paths.get(homeDir, "Library", "Application Support", "Code", "User", "settings.json")
                    .toString();
            } else if (Platform.isLinux()) {
                return Paths.get(homeDir, ".config", "Code", "User", "settings.json").toString();
            } else {
                throw new CredentialUnavailableException(PLATFORM_NOT_SUPPORTED_ERROR);
            }
        } catch (InvalidPathException e) {
            return null;
        }
    }

    /**
     * Get the user configured settings of Visual Studio code.
     *
     * @return a Map containing VS Code user settings
     */
    public Map<String, String> getUserSettingsDetails() {
        Map<String, String> details = null;
        String json = null;
        try {
            json = new String(Files.readAllBytes(Paths.get(getSettingsPath())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            details = jsonReader.readObject(reader -> {
                Map<String, String> result = new HashMap<>();
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("azure.cloud".equals(fieldName)) {
                        result.put("cloud", reader.getString());
                    } else if ("azure.tenant".equals(fieldName)) {
                        result.put("tenant", reader.getString());
                    } else {
                        reader.skipChildren();
                    }
                }
                return result;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!details.containsKey("cloud")) {
            details.put("cloud", "AzureCloud");
        }
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
