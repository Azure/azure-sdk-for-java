// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import org.apache.hc.core5.http.ClassicHttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.addTrailingSlashIfRequired;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * The REST client specific to getting an access token for Azure REST APIs.
 */
public final class AccessTokenUtil {
    /**
     * Stores the Client ID fragment.
     */
    private static final String CLIENT_ID_FRAGMENT = "&client_id=";

    /**
     * Stores the Client Secret fragment.
     */
    private static final String CLIENT_SECRET_FRAGMENT = "&client_secret=";

    /**
     * Stores the Grant Type fragment.
     */
    private static final String GRANT_TYPE_FRAGMENT = "grant_type=client_credentials";

    /**
     * Stores the Resource fragment.
     */
    private static final String RESOURCE_FRAGMENT = "&resource=";

    /**
     * Stores the OAuth2 token base URL.
     */
    private static final String OAUTH2_TOKEN_BASE_URL = "https://login.microsoftonline.com/";

    /**
     * Stores the OAuth2 token postfix.
     */
    private static final String OAUTH2_TOKEN_POSTFIX = "oauth2/token";

    /**
     * Stores the OAuth2 managed identity URL.
     */
    private static final String OAUTH2_MANAGED_IDENTITY_TOKEN_URL
        = "http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01";

    /**
     * A prefix to use on the bearer token header.
     */
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    /**
     * The WWW-Authenticate header name.
     */
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    /**
     * Stores our logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AccessTokenUtil.class.getName());

    private static final String PROPERTY_IDENTITY_ENDPOINT = "IDENTITY_ENDPOINT";
    private static final String PROPERTY_IDENTITY_HEADER = "IDENTITY_HEADER";
    private static final String PROPERTY_AZURE_FEDERATED_TOKEN_FILE = "AZURE_FEDERATED_TOKEN_FILE";
    private static final String PROPERTY_AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
    private static final String PROPERTY_AZURE_TENANT_ID = "AZURE_TENANT_ID";
    private static final String PROPERTY_AZURE_AUTHORITY_HOST = "AZURE_AUTHORITY_HOST";
    private static final String DEFAULT_AUTHORITY_HOST = "https://login.microsoftonline.com/";

    /**
     * Get an access token for a managed identity.
     *
     * @param resource The resource.
     * @param identity The user-assigned identity (null if system-assigned).
     *
     * @return The authorization token.
     */
    public static AccessToken getAccessToken(String resource, String identity) {
        AccessToken result;

        /*
         * Azure Workload Identity (AKS): AZURE_FEDERATED_TOKEN_FILE, AZURE_CLIENT_ID, AZURE_TENANT_ID
         * App Service 2017-09-01: MSI_ENDPOINT, MSI_SECRET
         * Azure Container App 2019-08-01: IDENTITY_ENDPOINT, IDENTITY_HEADER, see more from https://learn.microsoft.com/en-us/azure/container-apps/managed-identity?tabs=cli%2Chttp#rest-endpoint-reference
         * Azure Virtual Machine 2018-02-01, see more from https://learn.microsoft.com/en-us/entra/identity/managed-identities-azure-resources/how-to-use-vm-token#get-a-token-using-http
         */
        if (isWorkloadIdentityAvailable()) {
            result = getAccessTokenWithWorkloadIdentity(resource);
        } else if (System.getenv("WEBSITE_SITE_NAME") != null && !System.getenv("WEBSITE_SITE_NAME").isEmpty()) {
            result = getAccessTokenOnAppService(resource, identity);
        } else if (System.getenv(PROPERTY_IDENTITY_ENDPOINT) != null
            && !System.getenv(PROPERTY_IDENTITY_ENDPOINT).isEmpty()) {
            result = getAccessTokenOnContainerApp(resource, identity);
        } else {
            result = getAccessTokenOnOthers(resource, identity);
        }

        return result;
    }

    /**
     * Get an access token.
     *
     * @param resource The resource.
     * @param tenantId The tenant ID.
     * @param aadAuthenticationUrl The AAD authentication url.
     * @param clientId The client ID.
     * @param clientSecret The client secret.
     *
     * @return The authorization token.
     */
    public static AccessToken getAccessToken(String resource, String aadAuthenticationUrl, String tenantId,
        String clientId, String clientSecret) {
        LOGGER.entering("AccessTokenUtil", "getAccessToken",
            new Object[] { resource, tenantId, clientId, clientSecret });
        LOGGER.info("Getting access token using client ID / client secret");

        AccessToken result = null;

        StringBuilder oauth2Url = new StringBuilder();

        if (aadAuthenticationUrl == null) {
            oauth2Url.append(OAUTH2_TOKEN_BASE_URL).append(tenantId).append("/");
        } else {
            oauth2Url.append(addTrailingSlashIfRequired(aadAuthenticationUrl));
        }

        oauth2Url.append(OAUTH2_TOKEN_POSTFIX);

        String encodedClientSecret = "";

        try {
            encodedClientSecret = URLEncoder.encode(clientSecret, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(WARNING, "Failed to encode client secret for access token request", e);
        }

        StringBuilder requestBody = new StringBuilder();

        requestBody.append(GRANT_TYPE_FRAGMENT)
            .append(CLIENT_ID_FRAGMENT)
            .append(clientId)
            .append(CLIENT_SECRET_FRAGMENT)
            .append(encodedClientSecret)
            .append(RESOURCE_FRAGMENT)
            .append(resource);

        String body = HttpUtil.post(oauth2Url.toString(), requestBody.toString(), "application/x-www-form-urlencoded");

        if (body != null) {
            try {
                result = JsonConverterUtil.fromJson(AccessToken::fromJson, body);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Failed to parse access token response.", e);
            }
        }

        LOGGER.exiting("AccessTokenUtil", "getAccessToken", result);

        return result;
    }

    /**
     * Check if Azure Workload Identity environment is available.
     *
     * @return true if Workload Identity environment variables are present, false otherwise.
     */
    public static boolean isWorkloadIdentityAvailable() {
        return !isNullOrEmpty(System.getenv(PROPERTY_AZURE_FEDERATED_TOKEN_FILE))
            && !isNullOrEmpty(System.getenv(PROPERTY_AZURE_CLIENT_ID))
            && !isNullOrEmpty(System.getenv(PROPERTY_AZURE_TENANT_ID));
    }

    /**
     * Get the access token using Azure Workload Identity (AKS federated token).
     * This is a convenience method that uses environment variables for all parameters.
     *
     * @param resource The resource.
     * @return The authorization token.
     */
    private static AccessToken getAccessTokenWithWorkloadIdentity(String resource) {
        return getAccessTokenWithWorkloadIdentity(resource, null, null, null);
    }

    /**
     * Get an access token via
     * <a href="https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-client-creds-grant-flow#third-case-access-token-request-with-a-federated-credential">
     * client credentials grant flow</a> using
     * <a href="https://learn.microsoft.com/en-us/azure/aks/workload-identity-overview">
     * Microsoft Entra Workload ID with AKS</a>.
     * Uses the federated token in file located at the provided path and the given clientId and tenantId
     * to issue an access token HTTP request.
     *
     * @param resource The resource scope (will be appended with /.default if not already present).
     * @param tenantId Tenant ID to use. If blank, fallback to environment variable AZURE_TENANT_ID.
     * @param clientId Client ID of the managed identity to use. If blank, fallback to environment variable AZURE_CLIENT_ID.
     * @param tokenFilePath Path to the federated token file. If blank, fallback to environment variable AZURE_FEDERATED_TOKEN_FILE.
     * @return An access token, or null if the operation fails.
     */
    public static AccessToken getAccessTokenWithWorkloadIdentity(String resource, String tenantId, String clientId,
        String tokenFilePath) {
        LOGGER.entering("AccessTokenUtil", "getAccessTokenWithWorkloadIdentity",
            new Object[] { resource, tenantId, clientId, tokenFilePath });
        LOGGER.info("Getting access token using federated Workload Identity token");

        // Use environment variables as fallback
        String effectiveTokenFilePath
            = useDefaultIfBlank(tokenFilePath, () -> System.getenv(PROPERTY_AZURE_FEDERATED_TOKEN_FILE));
        String effectiveTenantId = useDefaultIfBlank(tenantId, () -> System.getenv(PROPERTY_AZURE_TENANT_ID));
        String effectiveClientId = useDefaultIfBlank(clientId, () -> System.getenv(PROPERTY_AZURE_CLIENT_ID));

        LOGGER.log(INFO, "Using federated token file: {0}", effectiveTokenFilePath);
        LOGGER.log(INFO, "Using clientId {0} in tenantId {1}", new Object[] { effectiveClientId, effectiveTenantId });

        // Ensure scope ends with /.default
        String scope = buildScope(resource);

        // Allow override of authority host via environment variable
        String authorityHost
            = useDefaultIfBlank(System.getenv(PROPERTY_AZURE_AUTHORITY_HOST), () -> DEFAULT_AUTHORITY_HOST);

        String federatedToken = readFile(effectiveTokenFilePath);
        if (isNullOrEmpty(federatedToken)) {
            LOGGER.log(WARNING, "Failed to read federated token from file: {0}", effectiveTokenFilePath);
            LOGGER.exiting("AccessTokenUtil", "getAccessTokenWithWorkloadIdentity", null);
            return null;
        }

        String requestUrl = buildTokenRequestUrl(authorityHost, effectiveTenantId);
        String requestBody = buildTokenRequestBody(effectiveClientId, federatedToken, scope);

        String response = HttpUtil.post(requestUrl, requestBody, "application/x-www-form-urlencoded");
        AccessToken result = parseAccessTokenResponse(response);

        LOGGER.exiting("AccessTokenUtil", "getAccessTokenWithWorkloadIdentity", result);
        return result;
    }

    /**
     * Builds the OAuth2 scope from the resource, ensuring it ends with /.default.
     *
     * @param resource The resource scope.
     * @return The formatted scope.
     */
    private static String buildScope(String resource) {
        if (resource.endsWith("/.default")) {
            return resource;
        }
        return addTrailingSlashIfRequired(resource) + ".default";
    }

    /**
     * Builds the token request URL for OAuth2.
     *
     * @param authorityHost The authority host.
     * @param tenantId The tenant ID.
     * @return The complete token request URL.
     */
    private static String buildTokenRequestUrl(String authorityHost, String tenantId) {
        return addTrailingSlashIfRequired(authorityHost) + tenantId + "/oauth2/v2.0/token";
    }

    /**
     * Builds the token request body for workload identity authentication.
     *
     * @param clientId The client ID.
     * @param federatedToken The federated token.
     * @param scope The OAuth2 scope.
     * @return The URL-encoded request body.
     */
    private static String buildTokenRequestBody(String clientId, String federatedToken, String scope) {
        return "grant_type=client_credentials" + "&client_id=" + urlEncode(clientId) + "&client_assertion_type="
            + urlEncode("urn:ietf:params:oauth:client-assertion-type:jwt-bearer") + "&client_assertion="
            + urlEncode(federatedToken) + "&scope=" + urlEncode(scope);
    }

    /**
     * Parse an access token from the HTTP response body.
     *
     * @param response The HTTP response body.
     * @return The parsed AccessToken, or null if parsing fails.
     */
    private static AccessToken parseAccessTokenResponse(String response) {
        if (response == null) {
            return null;
        }

        try {
            return JsonConverterUtil.fromJson(AccessToken::fromJson, response);
        } catch (IOException e) {
            LOGGER.log(WARNING, "Failed to parse access token from response.", e);
            return null;
        }
    }

    /**
     * Read file contents from the specified path.
     * Package-private for testing.
     *
     * @param filePath The path to the file.
     * @return The file content as a trimmed string, or null if reading fails.
     */
    static String readFile(String filePath) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            return new String(java.nio.file.Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            LOGGER.log(WARNING, "Failed to read file.", e);
            return null;
        }
    }

    /**
     * Get the access token on Azure App Service.
     *
     * @param resource The resource.
     * @param clientId The user-assigned managed identity (null if system-assigned).
     * @return The authorization token.
     */
    private static AccessToken getAccessTokenOnAppService(String resource, String clientId) {
        LOGGER.entering("AccessTokenUtil", "getAccessTokenOnAppService", resource);
        LOGGER.info("Getting access token using managed identity based on MSI_SECRET");

        AccessToken result = null;
        StringBuilder url = new StringBuilder();

        url.append(System.getenv("MSI_ENDPOINT"))
            .append("?api-version=2017-09-01")
            .append(RESOURCE_FRAGMENT)
            .append(resource);

        if (clientId != null) {
            url.append("&clientid=").append(clientId);

            LOGGER.log(INFO, "Using managed identity with client ID: {0}", clientId);
        }

        HashMap<String, String> headers = new HashMap<>();

        headers.put("Metadata", "true");
        headers.put("Secret", System.getenv("MSI_SECRET"));

        String body = HttpUtil.get(url.toString(), headers);

        if (body != null) {
            try {
                result = JsonConverterUtil.fromJson(AccessToken::fromJson, body);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Failed to parse access token response.", e);
            }
        }

        LOGGER.exiting("AccessTokenUtil", "getAccessTokenOnAppService", result);

        return result;
    }

    /**
     * Get the access token on Azure Container App (API version 2019-08-01)
     *
     * @param resource The resource.
     * @param clientId The user-assigned managed identity (null if system-assigned).
     * @return The authorization token.
     */
    private static AccessToken getAccessTokenOnContainerApp(String resource, String clientId) {
        LOGGER.entering("AccessTokenUtil", "getAccessTokenOnContainerApp", resource);
        LOGGER.info("Getting access token using managed identity.");

        AccessToken result = null;
        StringBuilder url = new StringBuilder();

        url.append(System.getenv(PROPERTY_IDENTITY_ENDPOINT))
            .append("?api-version=2019-08-01")
            .append(RESOURCE_FRAGMENT)
            .append(resource);

        if (clientId != null) {
            url.append("&client_id=").append(clientId);
            LOGGER.log(INFO, "Using managed identity with client ID: {0}", clientId);
        }

        Map<String, String> headers = new HashMap<>();
        if (System.getenv(PROPERTY_IDENTITY_HEADER) != null && !System.getenv(PROPERTY_IDENTITY_HEADER).isEmpty()) {
            headers.put("X-IDENTITY-HEADER", System.getenv(PROPERTY_IDENTITY_HEADER));
        }

        String body = HttpUtil.get(url.toString(), headers);

        if (body != null) {
            try {
                result = JsonConverterUtil.fromJson(AccessToken::fromJson, body);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Failed to parse access token response.", e);
            }
        }

        LOGGER.exiting("AccessTokenUtil", "getAccessTokenOnContainerApp", result);

        return result;
    }

    /**
     * Get the authorization token on everything else but Azure App Service.
     *
     * @param resource The resource.
     * @param identity The user-assigned identity (null if system-assigned).
     * @return The authorization token.
     */
    private static AccessToken getAccessTokenOnOthers(String resource, String identity) {
        LOGGER.entering("AccessTokenUtil", "getAccessTokenOnOthers", resource);
        LOGGER.info("Getting access token using managed identity");

        if (identity != null) {
            LOGGER.log(INFO, "Using managed identity with object ID: {0}", identity);
        }

        AccessToken result = null;

        StringBuilder url = new StringBuilder();

        url.append(OAUTH2_MANAGED_IDENTITY_TOKEN_URL).append(RESOURCE_FRAGMENT).append(resource);

        if (identity != null) {
            url.append("&object_id=").append(identity);
        }

        HashMap<String, String> headers = new HashMap<>();

        headers.put("Metadata", "true");

        String body = HttpUtil.get(url.toString(), headers);

        if (body != null) {
            try {
                result = JsonConverterUtil.fromJson(AccessToken::fromJson, body);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Failed to parse access token response.", e);
            }
        }

        LOGGER.exiting("AccessTokenUtil", "getAccessTokenOnOthers", result);

        return result;
    }

    public static String getLoginUri(String resourceUri, boolean disableChallengeResourceVerification) {
        LOGGER.entering("AccessTokenUtil", "getLoginUri", resourceUri);
        LOGGER.log(INFO, "Getting login URI using: {0}", resourceUri);

        ClassicHttpResponse response = HttpUtil.getWithResponse(resourceUri, null);

        if (response == null) {
            throw new IllegalStateException("Could not obtain login URI to retrieve access token from.");
        }

        Map<String, String> challengeAttributes
            = extractChallengeAttributes(response.getFirstHeader(WWW_AUTHENTICATE).getValue());
        String scope = challengeAttributes.get("resource");

        if (scope != null) {
            scope = scope + "/.default";
        } else {
            scope = challengeAttributes.get("scope");
        }

        if (scope == null) {
            return null;
        } else {
            if (!disableChallengeResourceVerification && !isChallengeResourceValid(resourceUri, scope)) {
                throw new IllegalStateException("The challenge resource " + scope + " does not match the requested "
                    + "domain. If you wish to disable this check, set the environment property "
                    + "'azure.keyvault.disable-challenge-resource-verification' to 'true'. See "
                    + "https://aka.ms/azsdk/blog/vault-uri for more information.");
            }

            String authorization = challengeAttributes.get("authorization");

            if (authorization == null) {
                authorization = challengeAttributes.get("authorization_uri");
            }

            try {
                new URI(authorization);

                LOGGER.log(INFO, "Obtained login URI: {0}", authorization);
                LOGGER.exiting("AccessTokenUtil", "getLoginUri", authorization);

                return authorization;
            } catch (URISyntaxException e) {
                throw new IllegalStateException("The challenge authorization URI " + authorization + " is invalid.", e);
            }
        }
    }

    /**
     * Extracts attributes off the bearer challenge in the authentication header.
     *
     * @param authenticateHeader The authentication header containing the challenge.
     *
     * @return A challenge attributes map.
     */
    private static Map<String, String> extractChallengeAttributes(String authenticateHeader) {
        LOGGER.entering("AccessTokenUtil", "extractChallengeAttributes", authenticateHeader);

        if (!isBearerChallenge(authenticateHeader)) {
            return Collections.emptyMap();
        }

        authenticateHeader
            = authenticateHeader.toLowerCase(Locale.ROOT).replace(BEARER_TOKEN_PREFIX.toLowerCase(Locale.ROOT), "");

        String[] attributes = authenticateHeader.split(", ");
        Map<String, String> attributeMap = new HashMap<>();

        for (String pair : attributes) {
            String[] keyValue = pair.split("=");

            attributeMap.put(keyValue[0].replaceAll("\"", ""), keyValue[1].replaceAll("\"", ""));
        }

        LOGGER.exiting("AccessTokenUtil", "extractChallengeAttributes", attributeMap);

        return attributeMap;
    }

    /**
     * Verifies whether a challenge is bearer or not.
     *
     * @param authenticateHeader The authentication header containing all the challenges.
     *
     * @return A boolean indicating if the challenge is a bearer challenge or not.
     */
    private static boolean isBearerChallenge(String authenticateHeader) {
        return authenticateHeader != null
            && !authenticateHeader.isEmpty()
            && authenticateHeader.toLowerCase(Locale.ROOT).startsWith(BEARER_TOKEN_PREFIX.toLowerCase(Locale.ROOT));
    }

    /**
     * Verifies whether a challenge resource is valid or not.
     *
     * @param resource The URI to validate the challenge against.
     * @param scope The scope of the challenge.
     *
     * @return A boolean indicating if the resource URI is valid or not.
     */
    private static boolean isChallengeResourceValid(String resource, String scope) {
        LOGGER.entering("AccessTokenUtil", "isChallengeResourceValid", new Object[] { resource, scope });

        final URI resourceUri;

        try {
            resourceUri = new URI(resource);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("The provided resource " + resource + " is not a valid URI.", e);
        }

        final URI scopeUri;

        try {
            scopeUri = new URI(scope);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("The challenge scope " + scope + " is not a valid URI.", e);
        }

        boolean isValid = resourceUri.getHost()
            .toLowerCase(Locale.ROOT)
            .endsWith("." + scopeUri.getHost().toLowerCase(Locale.ROOT));

        LOGGER.exiting("AccessTokenUtil", "isChallengeResourceValid", isValid);

        // Returns false if the host specified in the scope does not match the requested domain.
        return isValid;
    }

    /**
     * Checks if a string is null or empty.
     *
     * @param value The string to check.
     * @return true if the string is null or empty, false otherwise.
     */
    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * URL-encodes the given text using UTF-8 encoding.
     *
     * @param text The text to encode.
     * @return The URL-encoded text, or null if encoding fails.
     */
    private static String urlEncode(String text) {
        if (text == null) {
            return null;
        }

        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(WARNING, "Failed to encode text.", e);
            return null;
        }
    }

    /**
     * Returns the value if not null or blank, otherwise returns the default value from the supplier.
     *
     * @param value The value to check.
     * @param defaultValueSupplier The supplier for the default value.
     * @return The value if not null or blank, otherwise the default value.
     */
    private static String useDefaultIfBlank(String value, java.util.function.Supplier<String> defaultValueSupplier) {
        if (isNullOrEmpty(value)) {
            return defaultValueSupplier.get();
        }
        return value;
    }
}
