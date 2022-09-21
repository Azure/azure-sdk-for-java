// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.properties;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * Defines grant types: client_credentials, authorization_code, on_behalf_of, azure_delegated.
 *
 * @deprecated use {@link AuthorizationGrantType} instead.
 */
@Deprecated
public enum AadAuthorizationGrantType {

    /**
     * Client credentials
     */
    CLIENT_CREDENTIALS("client_credentials"),

    /**
     * Authorization code
     */
    AUTHORIZATION_CODE("authorization_code"),

    /**
     * On behalf of
     */
    ON_BEHALF_OF("on_behalf_of"),

    /**
     * Azure delegated
     */
    AZURE_DELEGATED("azure_delegated");

    private final String authorizationGrantType;

    AadAuthorizationGrantType(String authorizationGrantType) {
        // For backward compatibility, we support 'on-behalf-of'.
        if ("on-behalf-of".equals(authorizationGrantType)) {
            this.authorizationGrantType = "on_behalf_of";
        } else {
            this.authorizationGrantType = authorizationGrantType;
        }
    }

    /**
     * Gets the string representation of the enum.
     *
     * @return the string representation of the enum
     */
    public String getValue() {
        return authorizationGrantType;
    }

    /**
     * Whether the other grant type is the same as the Azure AD grant type.
     *
     * @param grantType the other grant type
     * @return whether the other grant type is the same as the Azure AD grant type
     */
    public boolean isSameGrantType(AuthorizationGrantType grantType) {
        return this.authorizationGrantType.equals(grantType.getValue());
    }
}
