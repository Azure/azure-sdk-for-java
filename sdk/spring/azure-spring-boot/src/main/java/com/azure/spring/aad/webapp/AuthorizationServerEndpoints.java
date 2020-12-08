// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.nimbusds.oauth2.sdk.util.StringUtils;

/**
 * Used to get endpoints for Microsoft Identity authorization server.
 */
public class AuthorizationServerEndpoints {

    private static final String IDENTITY_PLATFORM = "https://login.microsoftonline.com/";

    private static final String AUTHORIZATION_ENDPOINT = "/oauth2/v2.0/authorize";
    private static final String TOKEN_ENDPOINT = "/oauth2/v2.0/token";
    private static final String JWK_SET_ENDPOINT = "/discovery/v2.0/keys";
    private static final String END_SESSION_ENDPOINT = "/oauth2/v2.0/logout";

    private final String baseUri;

    public AuthorizationServerEndpoints() {
        this(IDENTITY_PLATFORM);
    }

    public AuthorizationServerEndpoints(String baseUri) {
        if (StringUtils.isBlank(baseUri)) {
            baseUri = IDENTITY_PLATFORM;
        }
        this.baseUri = addSlash(baseUri);
    }

    private String addSlash(String uri) {
        return uri.endsWith("/") ? uri : uri + "/";
    }

    public String authorizationEndpoint(String tenant) {
        return baseUri + tenant + AUTHORIZATION_ENDPOINT;
    }

    public String tokenEndpoint(String tenant) {
        return baseUri + tenant + TOKEN_ENDPOINT;
    }

    public String jwkSetEndpoint(String tenant) {
        return baseUri + tenant + JWK_SET_ENDPOINT;
    }

    public String endSessionEndpoint(String tenant) {
        return baseUri + tenant + END_SESSION_ENDPOINT;
    }
}
