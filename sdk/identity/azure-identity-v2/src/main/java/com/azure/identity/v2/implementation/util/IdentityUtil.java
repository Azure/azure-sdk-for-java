// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.util;

import com.azure.identity.v2.BrowserCustomizationOptions;
import com.azure.identity.v2.implementation.models.ClientOptions;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.clientcore.core.serialization.json.JsonToken.END_OBJECT;

public final class IdentityUtil {
    private static final ClientLogger LOGGER = new ClientLogger(IdentityUtil.class);
    public static final String ALL_TENANTS = "*";
    public static final String DEFAULT_TENANT = "organizations";
    public static final HttpHeaderName X_TFS_FED_AUTH_REDIRECT = HttpHeaderName.fromString("X-TFS-FedAuthRedirect");
    public static final HttpHeaderName X_VSS_E2EID = HttpHeaderName.fromString("x-vss-e2eid");
    public static final HttpHeaderName X_MSEDGE_REF = HttpHeaderName.fromString("x-msedge-ref");
    public static final String PROPERTY_AZURE_TENANT_ID = "AZURE_TENANT_ID";
    public static final String PROPERTY_AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
    public static final String PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH = "AZURE_CLIENT_CERTIFICATE_PATH";
    public static final String PROPERTY_AZURE_CLIENT_CERTIFICATE_PASSWORD = "AZURE_CLIENT_CERTIFICATE_PATH";
    public static final String PROPERTY_AZURE_CLIENT_SECRET = "AZURE_CLIENT_SECRET";
    public static final String AZURE_FEDERATED_TOKEN_FILE = "AZURE_FEDERATED_TOKEN_FILE";
    public static final String PROPERTY_AZURE_CLIENT_SEND_CERTIFICATE_CHAIN = "AZURE_CLIENT_SEND_CERTIFICATE_CHAIN";
    public static final String AZURE_ADDITIONALLY_ALLOWED_TENANTS = "AZURE_ADDITIONALLY_ALLOWED_TENANTS";
    public static final String PROPERTY_AZURE_AUTHORITY_HOST = "AZURE_AUTHORITY_HOST";
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
        ClientOptions options) {

        String contextTenantId = requestContext.getTenantId();

        if (contextTenantId != null && currentTenantId != null && !currentTenantId.equalsIgnoreCase(contextTenantId)) {

            String resolvedTenantId = CoreUtils.isNullOrEmpty(contextTenantId) ? currentTenantId : contextTenantId;

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

    /**
     * Parses the "access_token" field out of a response body.
     * @param json the response body to parse.
     * @return the access_token value
     * @throws IOException
     */
    public static String getAccessToken(String json) throws IOException {
        try (JsonReader jsonReader = JsonReader.fromString(json)) {
            return jsonReader.readObject(reader -> {
                while (reader.nextToken() != END_OBJECT) {
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
        try (JsonReader jsonReader = JsonReader.fromString(json)) {

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

    public static boolean isMacPlatform() {
        return System.getProperty("os.name").contains("Mac");
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

    public static List<String> getAdditionalTenantsFromEnvironment(Configuration configuration) {
        String additionalTenantsFromEnv = configuration.get(AZURE_ADDITIONALLY_ALLOWED_TENANTS);
        if (!CoreUtils.isNullOrEmpty(additionalTenantsFromEnv)) {
            return resolveAdditionalTenants(
                Arrays.asList(configuration.get(AZURE_ADDITIONALLY_ALLOWED_TENANTS).split(";")));
        } else {
            return Collections.emptyList();
        }
    }
}
