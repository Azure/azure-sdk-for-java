package com.azure.identity;

public class OnBehalfOfCredentialBuilder extends AadCredentialBuilderBase<OnBehalfOfCredentialBuilder> {
    private String clientSecret;

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the AAD application.
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Configures the persistent shared token cache options and enables the persistent token cache which is disabled
     * by default. If configured, the credential will store tokens in a cache persisted to the machine, protected to
     * the current user, which can be shared by other credentials and processes.
     *
     * @param tokenCachePersistenceOptions the token cache configuration options
     * @return An updated instance of this builder with the token cache options configured.
     */
    public OnBehalfOfCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }
}
