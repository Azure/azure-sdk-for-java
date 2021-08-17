// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.nimbusds.oauth2.sdk.util.StringUtils;

/**
 * Used to get endpoints for Microsoft Identity authorization server.
 */
public class AADAuthorizationServerEndpoints {

    private static final String DEFAULT_BASE_URI = "https://login.microsoftonline.com/";

    private static final String AUTHORIZATION_ENDPOINT = "/oauth2/v2.0/authorize";
    private static final String TOKEN_ENDPOINT = "/oauth2/v2.0/token";
    private static final String JWK_SET_ENDPOINT = "/discovery/v2.0/keys";
    private static final String END_SESSION_ENDPOINT = "/oauth2/v2.0/logout";

    private final String baseUri;
    private final String tenantId;

    public AADAuthorizationServerEndpoints(String baseUri, String tenantId) {
        if (StringUtils.isBlank(baseUri)) {
            baseUri = DEFAULT_BASE_URI;
        }
        this.baseUri = addSlash(baseUri);
        this.tenantId = tenantId;
    }

    public String getBaseUri() {
        return this.baseUri;
    }

    private String addSlash(String uri) {
        return uri.endsWith("/") ? uri : uri + "/";
    }

    public String authorizationEndpoint() {
        return baseUri + tenantId + AUTHORIZATION_ENDPOINT;
    }

    public String tokenEndpoint() {
        return baseUri + tenantId + TOKEN_ENDPOINT;
    }

    public String jwkSetEndpoint() {
        return baseUri + tenantId + JWK_SET_ENDPOINT;
    }

    public String endSessionEndpoint() {
        return baseUri + tenantId + END_SESSION_ENDPOINT;
    }
}
