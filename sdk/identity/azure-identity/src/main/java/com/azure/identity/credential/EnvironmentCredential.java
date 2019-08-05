// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.implementation.annotation.Immutable;
import com.azure.core.util.configuration.BaseConfigurations;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

/**
 * A credential provider that provides token credentials based on environment
 * variables.
 */
@Immutable
public class EnvironmentCredential implements TokenCredential {
    private Configuration configuration;
    private final IdentityClientOptions identityClientOptions;
    private final ClientLogger logger = new ClientLogger(EnvironmentCredential.class);

    /**
     * Creates an instance of the default environment credential provider.
     * @param identityClientOptions the options for configuring the identity client
     */
    EnvironmentCredential(IdentityClientOptions identityClientOptions) {
        this.configuration = ConfigurationManager.getConfiguration().clone();
        this.identityClientOptions = identityClientOptions;
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        return Mono.fromSupplier(() -> {
            if (configuration.contains(BaseConfigurations.AZURE_CLIENT_ID)
                && configuration.contains(BaseConfigurations.AZURE_CLIENT_SECRET)
                && configuration.contains(BaseConfigurations.AZURE_TENANT_ID)) {
                // TODO: support other clouds
                return new ClientSecretCredential(configuration.get(BaseConfigurations.AZURE_TENANT_ID),
                    configuration.get(BaseConfigurations.AZURE_CLIENT_ID),
                    configuration.get(BaseConfigurations.AZURE_CLIENT_SECRET),
                    identityClientOptions);
            }
            // Other environment variables
            logger.logAndThrow(new ClientAuthenticationException("Cannot create any credentials with the current environment variables", null));
            return null;
        }).flatMap(cred -> cred.getToken(scopes));
    }
}
