// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;

import reactor.core.publisher.Mono;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
<<<<<<< HEAD
import com.azure.core.util.logging.ClientLogger;
=======
>>>>>>> 19d0ededf1d6793437a7df4005a40ed504e65e10

/**
 * A credential provider that provides token credentials based on Azure CLI
 * command.
 */
@Immutable
class AzureCliCredential implements TokenCredential {
    private final IdentityClient identityClient;
<<<<<<< HEAD
    private final ClientLogger logger = new ClientLogger(AzureCliCredential.class);
    
=======

>>>>>>> 19d0ededf1d6793437a7df4005a40ed504e65e10
    /**
     * Creates an AzureCliSecretCredential with default identity client options.
     * @param identityClientOptions the options to configure the identity client
     */
    AzureCliCredential(IdentityClientOptions identityClientOptions) {
        identityClient = new IdentityClientBuilder().identityClientOptions(identityClientOptions).build();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
<<<<<<< HEAD
        return identityClient.authenticateWithAzureCli(request).onErrorResume(t -> Mono.error(logger.logExceptionAsError(new RuntimeException(
            "AzureCliCredential authentication unavailable.",
            t))));
=======
        return identityClient.authenticateWithAzureCli(request);
>>>>>>> 19d0ededf1d6793437a7df4005a40ed504e65e10
    }
}
