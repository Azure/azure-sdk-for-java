// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * This class centralizes the logic for performing a client assertion authentication through an {@link IdentityClient}.
 */
public class ClientAssertionCredentialHelper implements TokenCredential {

    private static final ClientLogger LOGGER = new ClientLogger(ClientAssertionCredentialHelper.class);
    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;

    public ClientAssertionCredentialHelper(String clientId, String tenantId, IdentityClientOptions options, Supplier<String> clientAssertion) {
        IdentityClientBuilder builder = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientAssertionSupplier(clientAssertion)
            .identityClientOptions(options);

        identityClient = builder.build();
        identitySyncClient = builder.buildSyncClient();

    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return identityClient.authenticateWithConfidentialClientCache(request)
            .onErrorResume(t -> Mono.empty())
            .switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithConfidentialClient(request)))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request,
                error));
    }

    public AccessToken getTokenSync(TokenRequestContext request) {
        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClientCache(request);
            if (token != null) {
                LoggingUtil.logTokenSuccess(LOGGER, request);
                return token;
            }
        } catch (Exception e) { }

        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClient(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
    }
}
