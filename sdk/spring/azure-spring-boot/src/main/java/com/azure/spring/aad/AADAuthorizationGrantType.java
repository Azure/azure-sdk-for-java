// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

public enum AADAuthorizationGrantType {

    CLIENT_CREDENTIALS("client_credentials"),
    PASSWORD("password"),
    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token"),
    ON_BEHALF_OF("on-behalf-of");

    private String authorizationGrantType;

    AADAuthorizationGrantType(String authorizationGrantType) {
        this.authorizationGrantType = authorizationGrantType;
    }

    public String getValue() {
        return authorizationGrantType;
    }
}
