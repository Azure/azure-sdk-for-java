// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security.properties;

import com.nimbusds.oauth2.sdk.util.StringUtils;

/**
 * Used to get endpoints for Microsoft Identity authorization server.
 */
public class AadAuthorizationServerEndpoints {

    private static final String DEFAULT_BASE_URI = "https://login.microsoftonline.com/";

    private static final String AUTHORIZATION_ENDPOINT = "/oauth2/v2.0/authorize";
    private static final String TOKEN_ENDPOINT = "/oauth2/v2.0/token";
    private static final String JWK_SET_ENDPOINT = "/discovery/v2.0/keys";
    private static final String END_SESSION_ENDPOINT = "/oauth2/v2.0/logout";

    private final String baseUri;
    private final String tenantId;

    /**
     * Creates a new instance of {@link AadAuthorizationServerEndpoints}.
     *
     * @param baseUri the base URI
     * @param tenantId the tenant ID
     */
    public AadAuthorizationServerEndpoints(String baseUri, String tenantId) {
        if (StringUtils.isBlank(baseUri)) {
            baseUri = DEFAULT_BASE_URI;
        }
        this.baseUri = addSlash(baseUri);
        this.tenantId = tenantId;
    }

    /**
     * Gets the base URI.
     *
     * @return the base URI
     */
    public String getBaseUri() {
        return this.baseUri;
    }

    private String addSlash(String uri) {
        return uri.endsWith("/") ? uri : uri + "/";
    }

    /**
     * Gets the authorization endpoint.
     *
     * @return the authorization endpoint
     */
    public String getAuthorizationEndpoint() {
        return baseUri + tenantId + AUTHORIZATION_ENDPOINT;
    }

    /**
     * Gets the token endpoint.
     *
     * @return the token endpoint
     */
    public String getTokenEndpoint() {
        return baseUri + tenantId + TOKEN_ENDPOINT;
    }

    /**
     * Gets the JWK set endpoint.
     *
     * @return the JWK set endpoint
     */
    public String getJwkSetEndpoint() {
        return baseUri + tenantId + JWK_SET_ENDPOINT;
    }

    /**
     * Gets the end session endpoint.
     *
     * @return the end session endpoint
     */
    public String getEndSessionEndpoint() {
        return baseUri + tenantId + END_SESSION_ENDPOINT;
    }
}
