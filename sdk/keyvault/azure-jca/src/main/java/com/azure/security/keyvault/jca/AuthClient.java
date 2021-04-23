// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.model.OAuthToken;

import java.util.HashMap;
import java.util.logging.Logger;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;

/**
 * The REST client specific to getting an access token for Azure REST APIs.
 */
class AuthClient extends DelegateRestClient {

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
    private static final String OAUTH2_TOKEN_POSTFIX = "/oauth2/token";

    /**
     * Stores the OAuth2 managed identity URL.
     */
    private static final String OAUTH2_MANAGED_IDENTITY_TOKEN_URL
            = "http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01";

    /**
     * Stores our logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AuthClient.class.getName());

    /**
     * Constructor.
     *
     * <p>
     * The constructor creates a default RestClient.
     * </p>
     */
    AuthClient() {
        super(RestClientFactory.createClient());
    }

    /**
     * Get an access token for a managed identity.
     *
     * @param resource the resource.
     * @param identity the user-assigned identity (null if system-assigned)
     * @return the authorization token.
     */
    public String getAccessToken(String resource, String identity) {
        String result;

        if (System.getenv("WEBSITE_SITE_NAME") != null
            && !System.getenv("WEBSITE_SITE_NAME").isEmpty()) {
            result = getAccessTokenOnAppService(resource, identity);
        } else {
            result = getAccessTokenOnOthers(resource, identity);
        }
        return result;
    }

    /**
     * Get an access token.
     *
     * @param resource the resource.
     * @param tenantId the tenant ID.
     * @param clientId the client ID.
     * @param clientSecret the client secret.
     * @return the authorization token.
     */
    public String getAccessToken(String resource, String aadAuthenticationUrl,
            String tenantId, String clientId, String clientSecret) {
        
        LOGGER.entering("AuthClient", "getAccessToken", new Object[]{
            resource, tenantId, clientId, clientSecret});
        LOGGER.info("Getting access token using client ID / client secret");
        String result = null;

        StringBuilder oauth2Url = new StringBuilder();
        oauth2Url.append(aadAuthenticationUrl == null ? OAUTH2_TOKEN_BASE_URL : aadAuthenticationUrl)
                .append(tenantId)
                .append(OAUTH2_TOKEN_POSTFIX);

        StringBuilder requestBody = new StringBuilder();
        requestBody.append(GRANT_TYPE_FRAGMENT)
                .append(CLIENT_ID_FRAGMENT).append(clientId)
                .append(CLIENT_SECRET_FRAGMENT).append(clientSecret)
                .append(RESOURCE_FRAGMENT).append(resource);

        String body = post(oauth2Url.toString(), requestBody.toString(), "application/x-www-form-urlencoded");
        if (body != null) {
            JsonConverter converter = JsonConverterFactory.createJsonConverter();
            OAuthToken token = (OAuthToken) converter.fromJson(body, OAuthToken.class);
            result = token.getAccessToken();
        }
        LOGGER.log(FINER, "Access token: {0}", result);
        return result;
    }

    /**
     * Get the access token on Azure App Service.
     *
     * @param resource the resource.
     * @param clientId the user-assigned managed identity (null if system-assigned).
     * @return the authorization token.
     */
    private String getAccessTokenOnAppService(String resource, String clientId) {
        LOGGER.entering("AuthClient", "getAccessTokenOnAppService", resource);
        LOGGER.info("Getting access token using managed identity based on MSI_SECRET");
        String result = null;

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
        String body = get(url.toString(), headers);

        if (body != null) {
            JsonConverter converter = JsonConverterFactory.createJsonConverter();
            OAuthToken token = (OAuthToken) converter.fromJson(body, OAuthToken.class);
            result = token.getAccessToken();
        }
        LOGGER.exiting("AuthClient", "getAccessTokenOnAppService", result);
        return result;
    }

    /**
     * Get the authorization token on everything else but Azure App Service.
     *
     * @param resource the resource.
     * @param identity the user-assigned identity (null if system-assigned).
     * @return the authorization token.
     */
    private String getAccessTokenOnOthers(String resource, String identity) {
        LOGGER.entering("AuthClient", "getAccessTokenOnOthers", resource);
        LOGGER.info("Getting access token using managed identity");
        if (identity != null) {
            LOGGER.log(INFO, "Using managed identity with object ID: {0}", identity);
        }
        String result = null;

        StringBuilder url = new StringBuilder();
        url.append(OAUTH2_MANAGED_IDENTITY_TOKEN_URL)
           .append(RESOURCE_FRAGMENT).append(resource);
        if (identity != null) {
            url.append("&object_id=").append(identity);
        }

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Metadata", "true");
        String body = get(url.toString(), headers);

        if (body != null) {
            JsonConverter converter = JsonConverterFactory.createJsonConverter();
            OAuthToken token = (OAuthToken) converter.fromJson(body, OAuthToken.class);
            result = token.getAccessToken();
        }
        LOGGER.exiting("AuthClient", "getAccessTokenOnOthers", result);
        return result;
    }
}
