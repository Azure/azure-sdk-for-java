// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Authenticates a service principal with AAD using a client assertion.
 */
@Immutable
public class ClientAssertionCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ClientAssertionCredential.class);
    private final IdentityClient identityClient;
    /**
     * Creates an instance of ClientAssertionCredential.
     *
     * @param clientId the client ID of user assigned or system assigned identity.
     * @param tenantId the tenant ID of the application
     * @param clientAssertion the supplier of the client assertion
     * @param identityClientOptions the options to configure the identity client
     */
    ClientAssertionCredential(String clientId, String tenantId, Supplier<String> clientAssertion,
                              IdentityClientOptions identityClientOptions) {
        identityClient = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientAssertionSupplier(clientAssertion)
            .identityClientOptions(identityClientOptions)
            .build();
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
}
