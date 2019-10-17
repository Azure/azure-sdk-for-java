// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequest;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

/**
 * A credential provider that provides token credentials based on environment
 * variables.
 */
@Immutable
public class EnvironmentCredential implements TokenCredential {
    private final Configuration configuration;
    private final IdentityClientOptions identityClientOptions;
    private final ClientLogger logger = new ClientLogger(EnvironmentCredential.class);

    /**
     * Creates an instance of the default environment credential provider.
     * @param identityClientOptions the options for configuring the identity client
     */
    EnvironmentCredential(IdentityClientOptions identityClientOptions) {
        this.configuration = Configuration.getGlobalConfiguration().clone();
        this.identityClientOptions = identityClientOptions;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequest request) {
        return Mono.fromSupplier(() -> {
            if (configuration.contains(Configuration.PROPERTY_AZURE_CLIENT_ID)
                && configuration.contains(Configuration.PROPERTY_AZURE_CLIENT_SECRET)
                && configuration.contains(Configuration.PROPERTY_AZURE_TENANT_ID)) {
                // TODO: support other clouds
                return new ClientSecretCredential(configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID),
                    configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID),
                    configuration.get(Configuration.PROPERTY_AZURE_CLIENT_SECRET),
                    identityClientOptions);
            }

            // Other environment variables
            throw logger.logExceptionAsError(new ClientAuthenticationException(
                "Cannot create any credentials with the current environment variables",
                null));
        }).flatMap(cred -> cred.getToken(request));
    }
}
