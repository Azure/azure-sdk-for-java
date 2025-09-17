// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.AuthenticationRecord;
import com.azure.identity.BrowserCustomizationOptions;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class IdentityUtil {
    public static final Path VSCODE_AUTH_RECORD_PATH = Paths.get(System.getProperty("user.home"), ".azure",
        "ms-azuretools.vscode-azureresourcegroups", "authRecord.json");
    private static final ClientLogger LOGGER = new ClientLogger(IdentityUtil.class);
    public static final String AZURE_ADDITIONALLY_ALLOWED_TENANTS = "AZURE_ADDITIONALLY_ALLOWED_TENANTS";
    public static final String ALL_TENANTS = "*";
    public static final String DEFAULT_TENANT = "organizations";
    public static final HttpHeaderName X_TFS_FED_AUTH_REDIRECT = HttpHeaderName.fromString("X-TFS-FedAuthRedirect");
    public static final HttpHeaderName X_VSS_E2EID = HttpHeaderName.fromString("x-vss-e2eid");
    public static final HttpHeaderName X_MSEDGE_REF = HttpHeaderName.fromString("x-msedge-ref");

    public static final File NULL_FILE
        = new File((System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null"));

    private IdentityUtil() {
    }

    /**
     * Resolve the Tenant Id to be used in the authentication requests.
     * @param currentTenantId the current tenant Id.
     * @param requestContext the user passed in {@link TokenRequestContext}
     * @param options the identity client options bag.
     * on the credential or not.
     */
    public static String resolveTenantId(String currentTenantId, TokenRequestContext requestContext,
        IdentityClientOptions options) {

        String contextTenantId = requestContext.getTenantId();

        if (contextTenantId != null && currentTenantId != null && !currentTenantId.equalsIgnoreCase(contextTenantId)) {
            if (options.isMultiTenantAuthenticationDisabled()) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("The Multi Tenant Authentication "
                    + "is disabled. An updated Tenant Id provided via TokenRequestContext cannot be used in this "
                    + "scenario. To resolve this issue, set the env var AZURE_IDENTITY_DISABLE_MULTITENANTAUTH"
                    + " to false ", null));
            } else if ("adfs".equals(currentTenantId)) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("The credential is configured with"
                    + "`adfs` tenant id and it cannot be replaced with a tenant id challenge provided via "
                    + "TokenRequestContext class. ", null));
            }
            String resolvedTenantId = CoreUtils.isNullOrEmpty(contextTenantId) ? currentTenantId : contextTenantId;

            if (!resolvedTenantId.equalsIgnoreCase(currentTenantId)
                && !options.getAdditionallyAllowedTenants().contains(ALL_TENANTS)
                && !options.getAdditionallyAllowedTenants().contains(resolvedTenantId)) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException(
                    "The current credential is not configured to acquire tokens for tenant " + resolvedTenantId
                        + ". To enable acquiring tokens for this tenant add it to the AdditionallyAllowedTenants on the credential options, "
                        + "or add \"*\" to AdditionallyAllowedTenants to allow acquiring tokens for any tenant. See the troubleshooting guide for more information. https://aka.ms/azsdk/java/identity/multitenant/troubleshoot",
                    null));
            }
            return resolvedTenantId;
        }

        return currentTenantId;

    }

    public static List<String> resolveAdditionalTenants(List<String> additionallyAllowedTenants) {
        if (additionallyAllowedTenants == null) {
            return Collections.emptyList();
        }

        if (additionallyAllowedTenants.contains(ALL_TENANTS)) {
            return Collections.singletonList(ALL_TENANTS);
        }

        return additionallyAllowedTenants;
    }

    public static List<String> getAdditionalTenantsFromEnvironment(Configuration configuration) {
        String additionalTenantsFromEnv = configuration.get(AZURE_ADDITIONALLY_ALLOWED_TENANTS);
        if (!CoreUtils.isNullOrEmpty(additionalTenantsFromEnv)) {
            return resolveAdditionalTenants(
                Arrays.asList(configuration.get(AZURE_ADDITIONALLY_ALLOWED_TENANTS).split(";")));
        } else {
            return Collections.emptyList();
        }
    }

    public static boolean browserCustomizationOptionsPresent(BrowserCustomizationOptions browserCustomizationOptions) {
        return !CoreUtils.isNullOrEmpty(browserCustomizationOptions.getErrorMessage())
            || !CoreUtils.isNullOrEmpty(browserCustomizationOptions.getSuccessMessage());
    }

    public static byte[] convertInputStreamToByteArray(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        try {
            int read = inputStream.read(buffer, 0, buffer.length);
            while (read != -1) {
                outputStream.write(buffer, 0, read);
                read = inputStream.read(buffer, 0, buffer.length);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return outputStream.toByteArray();
    }

    /**
     * Parses the "access_token" field out of a response body.
     * @param json the response body to parse.
     * @return the access_token value
     * @throws IOException
     */
    public static String getAccessToken(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return jsonReader.readObject(reader -> {
                while (reader.nextToken() != com.azure.json.JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("access_token".equals(fieldName)) {
                        return reader.getString();
                    }
                }
                return null;
            });
        }
    }

    /**
     * Parses a json string into a key:value map. Doesn't do anything smart for nested objects or arrays.
     * @param json
     * @return a map of the json fields
     * @throws IOException
     */
    public static Map<String, String> parseJsonIntoMap(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {

            return jsonReader.readObject(reader -> jsonReader.readMap(mapReader -> {
                if (mapReader.currentToken() == JsonToken.START_ARRAY
                    || mapReader.currentToken() == JsonToken.START_OBJECT) {
                    return mapReader.readChildren();
                } else {
                    return mapReader.getString();
                }
            }));
        }
    }

    public static boolean isWindowsPlatform() {
        return System.getProperty("os.name").contains("Windows");
    }

    public static boolean isLinuxPlatform() {
        return System.getProperty("os.name").contains("Linux");
    }

    public static boolean isVsCodeBrokerAuthAvailable() {
        // Check if VS Code broker auth record file exists
        File authRecordFile = VSCODE_AUTH_RECORD_PATH.toFile();
        return isBrokerAvailable() && authRecordFile.exists() && authRecordFile.isFile();
    }

    public static boolean isBrokerAvailable() {
        try {
            // 1. Check if Broker dependency is available
            Class.forName("com.azure.identity.broker.InteractiveBrowserBrokerCredentialBuilder");
            return true;
        } catch (ClassNotFoundException e) {
            return false; // Broker not present
        }
    }

    public static AuthenticationRecord loadVSCodeAuthRecord() throws IOException {
        // Resolve the full path to authRecord.json
        File file = VSCODE_AUTH_RECORD_PATH.toFile();
        if (!file.exists()) {
            return null;
        }
        // Read file content
        InputStream json = Files.newInputStream(VSCODE_AUTH_RECORD_PATH);
        // Deserialize to AuthenticationRecord
        return AuthenticationRecord.deserialize(json);
    }

    /**
     * Checks if the GNOME Keyring is accessible.
     * @return true if accessible, false otherwise.
     */
    public static boolean isKeyRingAccessible() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("secret-tool", "lookup", "test", "test");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return true; // Keyring is accessible
            } else {
                LOGGER.verbose("GNOME Keyring is unavailable or inaccessible.");
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted state if InterruptedException occurs
            LOGGER.verbose("Error while checking GNOME Keyring availability: " + e.getMessage());
            return false;
        } catch (IOException e) {
            LOGGER.verbose("Error while checking GNOME Keyring availability: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ensures the claims string is base64 encoded.
     * 
     * @param claims The claims string to encode if needed
     * @return Base64 encoded claims string
     */
    public static String ensureBase64Encoded(String claims) {
        if (claims == null || claims.trim().isEmpty()) {
            return claims;
        }
        return java.util.Base64.getEncoder().encodeToString(claims.getBytes(StandardCharsets.UTF_8));
    }
}
