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

import java.util.Objects;

/**
 * An AAD credential that acquires a token with a client secret for an AAD application.
 *
 * <p><strong>Sample: Construct a simple ClientSecretCredential</strong></p>
 * {@codesnippet com.azure.identity.credential.clientsecretcredential.construct}
 *
 * <p><strong>Sample: Construct a ClientSecretCredential behind a proxy</strong></p>
 * {@codesnippet com.azure.identity.credential.clientsecretcredential.constructwithproxy}
 */
@Immutable
public class ClientSecretCredential implements TokenCredential {
    private final IdentityClient identityClient;
    private final ClientLogger logger = new ClientLogger(ClientSecretCredential.class);

    /**
     * Creates a ClientSecretCredential with the given identity client options.
     *
     * @param tenantId the tenant ID of the application
     * @param clientId the client ID of the application
     * @param clientSecret the secret value of the AAD application.
     * @param identityClientOptions the options for configuring the identity client
     */
    ClientSecretCredential(String tenantId, String clientId, String clientSecret,
                           IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(clientSecret, "'clientSecret' cannot be null.");
        Objects.requireNonNull(identityClientOptions, "'identityClientOptions' cannot be null.");
        identityClient = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .identityClientOptions(identityClientOptions)
            .build();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return identityClient.authenticateWithConfidentialClientCache(request)
            .onErrorResume(t -> Mono.empty())
            .switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithConfidentialClient(request)))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(logger, request))
            .doOnError(error -> LoggingUtil.logTokenError(logger, request, error));
    }

    TokenCredentialType getCredentialType() {
        return TokenCredentialType.CLIENT_SECRET_CREDENTIAL;
    }
}
