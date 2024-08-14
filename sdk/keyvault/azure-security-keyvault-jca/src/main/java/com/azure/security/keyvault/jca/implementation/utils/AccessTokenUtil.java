// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import org.apache.http.HttpResponse;

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
import static java.util.logging.Level.FINER;
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

        if (System.getenv("WEBSITE_SITE_NAME") != null && !System.getenv("WEBSITE_SITE_NAME").isEmpty()) {
            result = getAccessTokenOnAppService(resource, identity);
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
            LOGGER.warning("Failed to encode client secret for access token request");
        }

        StringBuilder requestBody = new StringBuilder();

        requestBody.append(GRANT_TYPE_FRAGMENT)
            .append(CLIENT_ID_FRAGMENT).append(clientId)
            .append(CLIENT_SECRET_FRAGMENT).append(encodedClientSecret)
            .append(RESOURCE_FRAGMENT).append(resource);

        String body =
            HttpUtil.post(oauth2Url.toString(), requestBody.toString(), "application/x-www-form-urlencoded");

        if (body != null) {
            try {
                result = (AccessToken) JsonConverterUtil.fromJson(AccessToken.class, body);
            } catch (Throwable t) {
                LOGGER.log(WARNING, "Failed to parse access token response");
            }
        }

        LOGGER.log(FINER, "Access token: {0}", result);

        return result;
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
            .append(RESOURCE_FRAGMENT).append(resource);

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
                result = (AccessToken) JsonConverterUtil.fromJson(AccessToken.class, body);
            } catch (Throwable t) {
                LOGGER.log(WARNING, "Failed to parse access token response");
            }
        }

        LOGGER.exiting("AccessTokenUtil", "getAccessTokenOnAppService", result);

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

        url.append(OAUTH2_MANAGED_IDENTITY_TOKEN_URL)
           .append(RESOURCE_FRAGMENT).append(resource);

        if (identity != null) {
            url.append("&object_id=").append(identity);
        }

        HashMap<String, String> headers = new HashMap<>();

        headers.put("Metadata", "true");

        String body = HttpUtil.get(url.toString(), headers);

        if (body != null) {
            try {
                result = (AccessToken) JsonConverterUtil.fromJson(AccessToken.class, body);
            } catch (Throwable t) {
                LOGGER.log(WARNING, "Failed to parse access token response");
            }
        }

        LOGGER.exiting("AccessTokenUtil", "getAccessTokenOnOthers", result);

        return result;
    }

    public static String getLoginUri(String resourceUri, boolean disableChallengeResourceVerification) {
        LOGGER.entering("AccessTokenUtil", "getLoginUri", resourceUri);
        LOGGER.log(INFO, "Getting login URI using: {0}", resourceUri);

        HttpResponse response = HttpUtil.getWithResponse(resourceUri, null);

        if (response == null) {
            throw new IllegalStateException("Could not obtain login URI to retrieve access token from.");
        }

        Map<String, String> challengeAttributes =
            extractChallengeAttributes(response.getFirstHeader(WWW_AUTHENTICATE).getValue());
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
        if (!isBearerChallenge(authenticateHeader)) {
            return Collections.emptyMap();
        }

        authenticateHeader =
            authenticateHeader.toLowerCase(Locale.ROOT).replace(BEARER_TOKEN_PREFIX.toLowerCase(Locale.ROOT), "");

        String[] attributes = authenticateHeader.split(", ");
        Map<String, String> attributeMap = new HashMap<>();

        for (String pair : attributes) {
            String[] keyValue = pair.split("=");

            attributeMap.put(keyValue[0].replaceAll("\"", ""), keyValue[1].replaceAll("\"", ""));
        }

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
        return authenticateHeader != null && !authenticateHeader.isEmpty()
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

        // Returns false if the host specified in the scope does not match the requested domain.
        return resourceUri.getHost().toLowerCase(Locale.ROOT)
            .endsWith("." + scopeUri.getHost().toLowerCase(Locale.ROOT));
    }
}
