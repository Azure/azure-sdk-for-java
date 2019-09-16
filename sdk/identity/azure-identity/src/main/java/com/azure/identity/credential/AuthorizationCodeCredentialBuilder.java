// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.identity.implementation.util.ValidationUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Fluent credential builder for instantiating a {@link AuthorizationCodeCredential}.
 *
 * @see AuthorizationCodeCredential
 */
public class AuthorizationCodeCredentialBuilder extends AadCredentialBuilderBase<AuthorizationCodeCredentialBuilder> {
    private String authCode;
    private String redirectUri;

    /**
     * Sets the authorization code on the builder. This is required for building an
     * {@link AuthorizationCodeCredential}, but is not required for constructing the
     * login URL.
     *
     * @param authCode the authorization code acquired from user login
     * @return the AuthorizationCodeCredentialBuilder itself
     */
    public AuthorizationCodeCredentialBuilder authorizationCode(String authCode) {
        this.authCode = authCode;
        return this;
    }

    /**
     * Sets redirect URL for the Oauth 2.0 login request, which must be
     * registered as a valid reply URL on the application. the authorization code
     * will be sent to this URL so it must be listening on this server and is able
     * to complete the {@link AuthorizationCodeCredential} construction from there.
     *
     * @param redirectUri the redirect URI to send the authorization code
     * @return the AuthorizationCodeCredentialBuilder itself
     */
    public AuthorizationCodeCredentialBuilder redirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    /**
     * @return a {@link AuthorizationCodeCredential} with the current configurations.
     */
    public AuthorizationCodeCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("authorizationCode", authCode);
                put("redirectUri", redirectUri);
            }});
        try {
            return new AuthorizationCodeCredential(clientId, authCode, new URI(redirectUri), identityClientOptions);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds a login URL for the user to login on the client side.
     *
     * @param scopes the scopes the token will be used for
     * @return a login URL for the user to login
     */
    public String buildLoginUrl(String... scopes) {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
            put("clientId", clientId);
            put("redirectUri", redirectUri);
        }});
        return String.format("%s/oauth2/v2.0/authorize?response_type=code&response_mode=query&prompt"
                    + "=select_account&client_id=%s&redirect_uri=%s&state=%s&scope=%s",
                identityClientOptions.getAuthorityHost(), clientId, redirectUri, UUID.randomUUID(),
                String.join(" ", scopes));
    }
}
