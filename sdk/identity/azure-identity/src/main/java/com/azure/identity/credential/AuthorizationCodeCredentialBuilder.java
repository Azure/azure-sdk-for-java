// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link AuthorizationCodeCredential}.
 *
 * @see AuthorizationCodeCredential
 */
public class AuthorizationCodeCredentialBuilder extends AadCredentialBuilderBase<AuthorizationCodeCredentialBuilder> {
    private final ClientLogger logger = new ClientLogger(AuthorizationCodeCredentialBuilder.class);

    private String authCode;
    private String redirectUri;

    /**
     * Sets the authorization code on the builder.
     *
     * @param authCode the authorization code acquired from user login
     * @return the AuthorizationCodeCredentialBuilder itself
     */
    public AuthorizationCodeCredentialBuilder authorizationCode(String authCode) {
        this.authCode = authCode;
        return this;
    }

    /**
     * Sets redirect URI for the Oauth 2.0 login request, which must be
     * registered as a valid redirect URI on the application. the authorization code
     * will be sent to this URI so it must be listening on this server and is able
     * to complete the {@link AuthorizationCodeCredential} construction from there.
     * This is also called Reply URLs in some contexts.
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
            return new AuthorizationCodeCredential(clientId, tenantId, authCode,
                new URI(redirectUri), identityClientOptions);
        } catch (URISyntaxException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }
}
