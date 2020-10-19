// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License./

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.rest.OAuthToken;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.logging.Level.FINER;

/**
 * The REST client specific to getting an access token for Azure REST APIs.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
class AuthClient extends DelegateRestClient {

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
    public AuthClient() {
        super(RestClientFactory.createClient());
    }

    /**
     * Get an access token for a managed identity.
     *
     * @param resource the resource.
     * @return the authorization token.
     */
    public String getAccessToken(String resource) {
        String result;
        boolean siteNameSet = Optional.of("WEBSITE_SITE_NAME")
                                      .map(System::getenv)
                                      .map(String::trim)
                                      .filter(s -> !s.isEmpty())
                                      .isPresent();
        if (siteNameSet) {
            result = getAccessTokenOnAppService(resource);
        } else {
            result = getAccessTokenOnOthers(resource);
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
    public String getAccessToken(String resource,
                                 String tenantId,
                                 String clientId,
                                 String clientSecret) {
        LOGGER.entering(
            "AuthClient",
            "getAccessToken",
            new Object[] {
                resource,
                tenantId,
                clientId,
                clientSecret
            }
        );
        LOGGER.info("Getting access token using client ID / client secret");
        String result = null;
        String oauth2Url = String.format("https://login.microsoftonline.com/%s/oauth2/token", tenantId);
        String requestBody = String.format(
            "grant_type=client_credentials&client_id=%s&client_secret=%s&resource=%s",
            clientId,
            clientSecret,
            resource
        );
        String response = post(oauth2Url, requestBody, "application/x-www-form-urlencoded");
        if (response != null) {
            JsonConverter converter = JsonConverterFactory.createJsonConverter();
            OAuthToken token = (OAuthToken) converter.fromJson(response, OAuthToken.class);
            result = token.getAccess_token();
        }
        LOGGER.log(FINER, "Access token: {0}", result);
        return result;
    }

    /**
     * Get the access token on Azure App Service.
     *
     * @param resource the resource.
     * @return the authorization token.
     */
    private String getAccessTokenOnAppService(String resource) {
        LOGGER.entering("AuthClient", "getAccessTokenOnAppService", resource);
        LOGGER.info("Getting access token using managed identity based on MSI_SECRET");
        String result = null;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Metadata", "true");
        headers.put("Secret", System.getenv("MSI_SECRET"));
        String url = String.format("%s?api-version=2017-09-01&resource=%s", System.getenv("MSI_ENDPOINT"), resource);
        String response = get(url, headers);
        if (response != null) {
            JsonConverter converter = JsonConverterFactory.createJsonConverter();
            OAuthToken token = (OAuthToken) converter.fromJson(response, OAuthToken.class);
            result = token.getAccess_token();
        }
        LOGGER.exiting("AuthClient", "getAccessTokenOnAppService", result);
        return result;
    }

    /**
     * Get the authorization token on everything else but Azure App Service.
     *
     * @param resource the resource.
     * @return the authorization token.
     */
    private String getAccessTokenOnOthers(String resource) {
        LOGGER.entering("AuthClient", "getAccessTokenOnOthers", resource);
        LOGGER.info("Getting access token using managed identity");
        String result = null;
        String url = String.format(
            "http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01&resource=%s",
            resource
        );
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Metadata", "true");
        String body = get(url, headers);
        if (body != null) {
            JsonConverter converter = JsonConverterFactory.createJsonConverter();
            OAuthToken token = (OAuthToken) converter.fromJson(body, OAuthToken.class);
            result = token.getAccess_token();
        }
        LOGGER.exiting("AuthClient", "getAccessTokenOnOthers", result);
        return result;
    }
}
