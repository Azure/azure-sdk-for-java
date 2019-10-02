package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.cryptography.KeyEncryptionKey;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;

/**
 * An object capable of synchronously retrieving key encryption keys from a provided key identifier.
 */
public class KeyResolver implements com.azure.core.cryptography.KeyEncryptionKeyResolver {
    private CryptographyClientBuilder cryptographyClientBuilder;

    /**
     * Creates an instance of Key Resolver.
     */
    public KeyResolver() {
        cryptographyClientBuilder = new CryptographyClientBuilder();
    }

    /**
     * Creates an instance of Key Resolver.
     * @param credential The credential used to create {@link KeyEncryptionKey}
     */
    public KeyResolver(TokenCredential credential) {
        cryptographyClientBuilder = new CryptographyClientBuilder();
        cryptographyClientBuilder.credential(credential);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KeyEncryptionKey resolveKey(String keyId) {
        cryptographyClientBuilder.keyIdentifier(keyId);
        return cryptographyClientBuilder.buildClient();
    }

    /**
     * Sets the credential to use when authenticating HTTP requests.
     *
     * @param credential The credential to use for authenticating HTTP requests.
     * @return the updated {@link KeyResolver} object.
     * @throws NullPointerException if {@code credential} is {@code null}.
     */
    public KeyResolver credential(TokenCredential credential) {
        cryptographyClientBuilder.credential(credential);
        return this;
    }

    /**
     * Sets the logging level for HTTP requests and responses.
     *
     * <p>logLevel is optional. If not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
     * @return the updated {@link KeyResolver} object.
     * @throws NullPointerException if {@code logLevel} is {@code null}.
     */
    public KeyResolver httpLogDetailLevel(HttpLogDetailLevel logLevel) {
        cryptographyClientBuilder.httpLogDetailLevel(logLevel);
        return this;
    }

    /**
     * Adds a policy to the set of existing policies that are executed after {@link KeyEncryptionKey} required policies.
     *
     * @param policy The {@link HttpPipelinePolicy policy} to be added.
     * @return the updated {@link KeyResolver} object.
     * @throws NullPointerException if {@code policy} is {@code null}.
     */
    public KeyResolver addPolicy(HttpPipelinePolicy policy) {
        cryptographyClientBuilder.addPolicy(policy);
        return this;
    }

    /**
     * Sets the HTTP client to use for sending and receiving requests to and from the service.
     *
     * @param client The HTTP client to use for requests.
     * @return the updated {@link KeyResolver} object.
     * @throws NullPointerException If {@code client} is {@code null}.
     */
    public KeyResolver httpClient(HttpClient client) {
        cryptographyClientBuilder.httpClient(client);
        return this;
    }

    /**
     * Sets the HTTP pipeline to use for the service client.
     *
     * If {@code pipeline} is set, all other settings are ignored, aside from
     * key identifier to create {@link KeyEncryptionKey}.
     *
     * @param pipeline The HTTP pipeline to use for sending service requests and receiving responses.
     * @return the updated {@link KeyResolver} object.
     */
    public KeyResolver pipeline(HttpPipeline pipeline) {
        cryptographyClientBuilder.pipeline(pipeline);
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the key encryption key.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated {@link KeyResolver} object.
     */
    public KeyResolver configuration(Configuration configuration) {
        cryptographyClientBuilder.configuration(configuration);
        return this;
    }
}
