// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.IdentityClientOptions;
import reactor.core.publisher.Mono;

/**
 * A credential provider that provides token credentials based on environment
 * variables.
 */
public class EnvironmentCredential implements TokenCredential {
    private Configuration configuration;
    private final IdentityClientOptions identityClientOptions;

    /**
     * Creates an instance of the default environment credential provider.
     */
    public EnvironmentCredential() {
        this(new IdentityClientOptions());
    }

    /**
     * Creates an instance of the default environment credential provider.
     * @param identityClientOptions the options for configuring the identity client
     */
    public EnvironmentCredential(IdentityClientOptions identityClientOptions) {
        this.configuration = ConfigurationManager.getConfiguration();
        this.identityClientOptions = identityClientOptions;
    }

    @Override
    public Mono<String> getToken(String... scopes) {
        return Mono.fromSupplier(() -> {
            if (configuration.contains(BaseConfigurations.AZURE_CLIENT_ID)
                && configuration.contains(BaseConfigurations.AZURE_CLIENT_SECRET)
                && configuration.contains(BaseConfigurations.AZURE_TENANT_ID)) {
                // TODO: support other clouds
                return new ClientSecretCredential(identityClientOptions)
                    .clientId(configuration.get(BaseConfigurations.AZURE_CLIENT_ID))
                    .clientSecret(configuration.get(BaseConfigurations.AZURE_CLIENT_SECRET))
                    .tenantId(configuration.get(BaseConfigurations.AZURE_TENANT_ID));
            }
            // Other environment variables
            throw new ClientAuthenticationException("Cannot create any credentials with the current environment variables", null);
        }).flatMap(cred -> cred.getToken(scopes));
    }
}
