// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.credentials.oauth.OAuthTokenRequestContext;

import java.util.List;

/**
 * Auth Metadata represents the authentication related data provided at Http Request level to the pipeline policies.
 */
public class AuthMetadata {
    private List<String> authScheme;

    private OAuthTokenRequestContext oAuthTokenRequestContext;

    /** Creates an instance of the Auth Metadata.
     */
    public AuthMetadata() { }

    /**
     * Gets the supported auth schemes.
     *
     * @return the list containing supported auth schemes.
     */
    public List<String> getAuthScheme() {
        return authScheme;
    }

    /**
     * Configures the auth schemes to be used.
     *
     * @param authScheme the list of supported auth schemes.
     */
    public void setAuthScheme(List<String> authScheme) {
        this.authScheme = authScheme;
    }

    /**
     * Gets the OAuthTokenRequestContext associated at request level.
     *
     * @return the OAuthTokenRequestContext
     */
    public OAuthTokenRequestContext getoAuthTokenRequestContext() {
        return oAuthTokenRequestContext;
    }

    /**
     * Configures the OAuthTokenRequestContext to be used at request level.
     *
     * @param oAuthTokenRequestContext the OAuthTokenRequestContext to be configured.
     * @return the updated Auth Metadata instance.
     */
    public AuthMetadata setoAuthTokenRequestContext(OAuthTokenRequestContext oAuthTokenRequestContext) {
        this.oAuthTokenRequestContext = oAuthTokenRequestContext;
        return this;
    }
}
