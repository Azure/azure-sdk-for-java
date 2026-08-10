// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AadAuthorizationServerEndpointsTests {

    private static final String DEFAULT_BASE_URI = "https://login.microsoftonline.com/";
    private static final String CUSTOM_BASE_URI = "https://custom.endpoint.com/";
    private static final String TENANT_ID = "test-tenant-id";

    @Test
    void constructorWithNullBaseUri() {
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(null, TENANT_ID);
        assertEquals(DEFAULT_BASE_URI, endpoints.getBaseUri());
    }

    @Test
    void constructorWithEmptyBaseUri() {
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints("", TENANT_ID);
        assertEquals(DEFAULT_BASE_URI, endpoints.getBaseUri());
    }

    @Test
    void constructorWithWhitespaceBaseUri() {
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints("   ", TENANT_ID);
        assertEquals(DEFAULT_BASE_URI, endpoints.getBaseUri());
    }

    @Test
    void constructorWithValidBaseUri() {
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(CUSTOM_BASE_URI, TENANT_ID);
        assertEquals(CUSTOM_BASE_URI, endpoints.getBaseUri());
    }

    @Test
    void constructorWithBaseUriWithoutTrailingSlash() {
        String baseUriWithoutSlash = "https://custom.endpoint.com";
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(baseUriWithoutSlash, TENANT_ID);
        assertEquals(baseUriWithoutSlash + "/", endpoints.getBaseUri());
    }

    @Test
    void getAuthorizationEndpoint() {
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(DEFAULT_BASE_URI, TENANT_ID);
        String authEndpoint = endpoints.getAuthorizationEndpoint();
        assertEquals(DEFAULT_BASE_URI + TENANT_ID + "/oauth2/v2.0/authorize", authEndpoint);
    }

    @Test
    void getTokenEndpoint() {
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(DEFAULT_BASE_URI, TENANT_ID);
        String tokenEndpoint = endpoints.getTokenEndpoint();
        assertEquals(DEFAULT_BASE_URI + TENANT_ID + "/oauth2/v2.0/token", tokenEndpoint);
    }

    @Test
    void getJwkSetEndpoint() {
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(DEFAULT_BASE_URI, TENANT_ID);
        String jwkSetEndpoint = endpoints.getJwkSetEndpoint();
        assertEquals(DEFAULT_BASE_URI + TENANT_ID + "/discovery/v2.0/keys", jwkSetEndpoint);
    }

    @Test
    void getEndSessionEndpoint() {
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(DEFAULT_BASE_URI, TENANT_ID);
        String endSessionEndpoint = endpoints.getEndSessionEndpoint();
        assertEquals(DEFAULT_BASE_URI + TENANT_ID + "/oauth2/v2.0/logout", endSessionEndpoint);
    }
}
