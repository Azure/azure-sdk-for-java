// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License./
package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.rest.OAuthToken;

/**
 * The REST client specific to getting an access token for Azure REST APIs.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
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
     * Constructor.
     */
    public AuthClient() {
        super(RestClientFactory.createClient());
    }

    /**
     * Get an access token.
     *
     * @param resource the resource.
     * @param tenantId the tenant ID.
     * @param clientId the client ID.
     * @param clientSecret the client secret.
     * @return the access token.
     */
    public String getAuthorizationToken(String resource, String tenantId, 
            String clientId, String clientSecret) {
        
        String result = null;

        StringBuilder oauth2Url = new StringBuilder();
        oauth2Url.append(OAUTH2_TOKEN_BASE_URL)
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
            result = token.getAccess_token();
        }
        return result;
    }
}
