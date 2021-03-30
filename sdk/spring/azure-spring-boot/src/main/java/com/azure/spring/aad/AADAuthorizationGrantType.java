// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

/**
 * Defines grant types: client_credentials, authorization_code, on-behalf-of.
 */
public enum AADAuthorizationGrantType {

    CLIENT_CREDENTIALS("client_credentials"),
    AUTHORIZATION_CODE("authorization_code"),
    ON_BEHALF_OF("on-behalf-of");

    private String authorizationGrantType;

    AADAuthorizationGrantType(String authorizationGrantType) {
        this.authorizationGrantType = authorizationGrantType;
    }

    public String getValue() {
        return authorizationGrantType;
    }
}
