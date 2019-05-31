package com.azure.identity.credential;

import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.Configuration;
import com.azure.core.configuration.ConfigurationManager;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * A credential provider that provides token credentials based on environment
 * variables.
 */
public class EnvironmentCredential extends TokenCredential {
    private Configuration configuration;

    /**
     * Creates an instance of the default environment credential provider.
     */
    public EnvironmentCredential() {
        configuration = ConfigurationManager.getConfiguration();
    }

    @Override
    public Mono<String> getTokenAsync(List<String> scopes) {
        return Mono.fromSupplier(() -> {
            if (configuration.contains(BaseConfigurations.AZURE_CLIENT_ID)
                && configuration.contains(BaseConfigurations.AZURE_CLIENT_SECRET)
                && configuration.contains(BaseConfigurations.AZURE_TENANT_ID)) {
                // TODO: support other clouds
                return new ClientSecretCredential()
                    .clientId(configuration.get(BaseConfigurations.AZURE_CLIENT_ID))
                    .clientSecret(configuration.get(BaseConfigurations.AZURE_CLIENT_SECRET))
                    .tenantId(configuration.get(BaseConfigurations.AZURE_TENANT_ID));
            }
            // Other environment variables
            throw new ClientAuthenticationException("Cannot create any credentials with the current environment variables", null);
        }).flatMap(cred -> cred.getTokenAsync(scopes));
    }
}
