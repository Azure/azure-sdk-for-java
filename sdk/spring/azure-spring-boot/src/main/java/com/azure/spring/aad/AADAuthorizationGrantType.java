// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * Defines grant types: client_credentials, authorization_code, on_behalf_of.
 */
public enum AADAuthorizationGrantType {

    CLIENT_CREDENTIALS("client_credentials"),
    AUTHORIZATION_CODE("authorization_code"),
    ON_BEHALF_OF("on_behalf_of");

    private String authorizationGrantType;

    AADAuthorizationGrantType(String authorizationGrantType) {
        // Unified format, compatible with previous usage.
        if ("on-behalf-of".equals(authorizationGrantType)) {
            this.authorizationGrantType = "on_behalf_of";
        } else {
            this.authorizationGrantType = authorizationGrantType;
        }
    }

    public String getValue() {
        return authorizationGrantType;
    }

    public boolean isSameGrantType(AuthorizationGrantType grantType) {
        return this.authorizationGrantType.equals(grantType.getValue());
    }
}
