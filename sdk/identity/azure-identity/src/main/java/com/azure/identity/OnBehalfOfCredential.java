// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

/**
 * An AAD credential that acquires a token with a client secret and user assertion for an AAD application
 * on behalf of a user principal.
 */
public class OnBehalfOfCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(OnBehalfOfCredential.class);

    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;

    /**
     * Creates OnBehalfOfCredential with the specified AAD application details and client options.
     *
     * @param tenantId the tenant ID of the application
     * @param clientId the client ID of the application
     * @param clientSecret the secret value of the AAD application.
     * @param certificatePath the PEM file or PFX file containing the certificate
     * @param certificatePassword the password protecting the PFX file
     * @param identityClientOptions the options for configuring the identity client
     */
    OnBehalfOfCredential(String clientId, String tenantId, String clientSecret, String certificatePath,
                                String certificatePassword, IdentityClientOptions identityClientOptions) {
        IdentityClientBuilder builder = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .certificatePath(certificatePath)
            .certificatePassword(certificatePassword)
            .identityClientOptions(identityClientOptions);

        identitySyncClient = builder.buildSyncClient();
        identityClient = builder.build();

    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.deferContextual(ctx -> identityClient.authenticateWithConfidentialClientCache(request)
            .onErrorResume(t -> Mono.empty())
            .switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithOBO(request)))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(),
                request, error)));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClientCache(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) { }

        try {
            AccessToken token = identitySyncClient.authenticateWithOBO(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
    }

}
