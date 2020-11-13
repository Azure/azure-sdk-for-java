// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.nimbusds.oauth2.sdk.util.StringUtils;

/**
 * Util class used to create authorization server endpoints.
 */
public class AuthorizationServerEndpoints {

    private static final String DEFAULT_AUTHORIZATION_SERVER_URI = "https://login.microsoftonline.com/";
    private static final String AUTHORIZATION_ENDPOINT = "/oauth2/v2.0/authorize";
    private static final String TOKEN_ENDPOINT = "/oauth2/v2.0/token";
    private static final String JWK_SET_ENDPOINT = "/discovery/v2.0/keys";

    private final String baseUri;

    public AuthorizationServerEndpoints() {
        this(DEFAULT_AUTHORIZATION_SERVER_URI);
    }

    public AuthorizationServerEndpoints(String authorizationServerUri) {
        if (StringUtils.isBlank(authorizationServerUri)) {
            authorizationServerUri = DEFAULT_AUTHORIZATION_SERVER_URI;
        }
        this.baseUri = addSlash(authorizationServerUri);
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
}
