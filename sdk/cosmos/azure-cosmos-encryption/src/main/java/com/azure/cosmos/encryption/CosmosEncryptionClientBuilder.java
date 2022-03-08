// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;


import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.cryptography.KeyEncryptionKeyResolver;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

/**
 * Helper class to build {@link CosmosEncryptionAsyncClient} and {@link CosmosEncryptionClient}
 * instances as logical representation of the Azure Cosmos database service.
 * <p>
 * When building client, cosmosAsyncClient()/cosmosClient(), keyEncryptionKeyResolver() and keyEncryptionKeyResolverName() are mandatory APIs, without these the initialization will fail.
 * <pre>
 *     Building Cosmos Encryption Async Client APIs.
 *     If Azure key vault is used in {@link #keyEncryptionKeyResolver(KeyEncryptionKeyResolver)}, we can input {@link #KEY_RESOLVER_NAME_AZURE_KEY_VAULT} in {@link #keyEncryptionKeyResolverName(String)}
 *
 * {@code
 * CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = new CosmosEncryptionClientBuilder()
 *         .cosmosAsyncClient(cosmosAsyncClient)
 *         .keyEncryptionKeyResolver(keyEncryptionKeyResolver)
 *         .keyEncryptionKeyResolverName(keyEncryptionKeyResolverName)
 *         .buildAsyncClient();
 * }
 * </pre>
 *
 * <pre>
 *     Building Cosmos Encryption Sync Client minimal APIs
 *     If Azure key vault is used in {@link #keyEncryptionKeyResolver(KeyEncryptionKeyResolver)}, we can input {@link #KEY_RESOLVER_NAME_AZURE_KEY_VAULT} in {@link #keyEncryptionKeyResolverName(String)}
 *  * {@code
 * CosmosEncryptionClient client = new CosmosEncryptionClientBuilder()
 *         .cosmosClient(cosmosClient)
 *         .keyEncryptionKeyResolver(keyEncryptionKeyResolver)
 *         .keyEncryptionKeyResolverName(keyEncryptionKeyResolverName)
 *         .buildClient();
 * }
 * </pre>
 */
@ServiceClientBuilder(serviceClients = {CosmosEncryptionClient.class, CosmosEncryptionAsyncClient.class})
public class CosmosEncryptionClientBuilder {

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosClient cosmosClient;
    private KeyEncryptionKeyResolver keyEncryptionKeyResolver;
    private String keyEncryptionKeyResolverName;

    /**
     * KeyEncryptionKeyResolver name for {@link #keyEncryptionKeyResolverName(String)} if Azure key vault resolver is being used in {@link #keyEncryptionKeyResolver(KeyEncryptionKeyResolver)}.
     */
    public final static String KEY_RESOLVER_NAME_AZURE_KEY_VAULT = "AZURE_KEY_VAULT";

    /**
     * Instantiates a new Cosmos encryption client builder.
     */
    public CosmosEncryptionClientBuilder() {
    }

    /**
     * Sets the cosmos core async client to be used.
     *
     * @param cosmosAsyncClient cosmos async client
     * @return current CosmosEncryptionClientBuilder
     */
    public CosmosEncryptionClientBuilder cosmosAsyncClient(CosmosAsyncClient cosmosAsyncClient){
        this.cosmosAsyncClient = cosmosAsyncClient;
        return this;
    }

    /**
     * Sets the cosmos core sync client to be used.
     *
     * @param cosmosClient cosmos sync client
     * @return current CosmosEncryptionClientBuilder
     */
    public CosmosEncryptionClientBuilder cosmosClient(CosmosClient cosmosClient){
        this.cosmosClient = cosmosClient;
        return this;
    }

    /**
     * Sets the key wrap provider
     *
     * @param keyEncryptionKeyResolver custom keyEncryptionKeyResolver implementation of {@link KeyEncryptionKeyResolver}
     * @return current CosmosEncryptionClientBuilder
     */
    public CosmosEncryptionClientBuilder keyEncryptionKeyResolver(KeyEncryptionKeyResolver keyEncryptionKeyResolver) {
        this.keyEncryptionKeyResolver = keyEncryptionKeyResolver;
        return this;
    }

    /**
     * Sets the key encryption key resolver name
     *
     * @param keyEncryptionKeyResolverName  custom {@link KeyEncryptionKeyResolver} name
     * @return current CosmosEncryptionClientBuilder
     */
    public CosmosEncryptionClientBuilder keyEncryptionKeyResolverName(String keyEncryptionKeyResolverName) {
        this.keyEncryptionKeyResolverName = keyEncryptionKeyResolverName;
        return this;
    }

    /**
     * Builds a cosmos encryption async client.
     *
     * @return CosmosEncryptionAsyncClient Cosmos encryption async client
     */
    public CosmosEncryptionAsyncClient buildAsyncClient() {
        if(this.cosmosAsyncClient == null) {
            throw new IllegalArgumentException("CosmosAsyncClient has not been provided.");
        }

        if(this.keyEncryptionKeyResolver == null) {
            throw new IllegalArgumentException("KeyEncryptionKeyResolver has not been provided.");
        }

        if(StringUtils.isEmpty(this.keyEncryptionKeyResolverName)) {
            throw new IllegalArgumentException("KeyEncryptionKeyResolverName has not been provided.");
        }

        return new CosmosEncryptionAsyncClient(this.cosmosAsyncClient, this.keyEncryptionKeyResolver, this.keyEncryptionKeyResolverName);
    }

    /**
     * Builds a cosmos encryption async client.
     *
     * @return CosmosEncryptionAsyncClient Cosmos encryption async client
     */
    public CosmosEncryptionClient buildClient() {
        if(this.cosmosClient == null) {
            throw new IllegalArgumentException("CosmosClient has not been provided.");
        }

        if(this.keyEncryptionKeyResolver == null) {
            throw new IllegalArgumentException("KeyEncryptionKeyResolver has not been provided.");
        }

        if(StringUtils.isEmpty(this.keyEncryptionKeyResolverName)) {
            throw new IllegalArgumentException("KeyEncryptionKeyResolverName has not been provided.");
        }

        return new CosmosEncryptionClient(this.cosmosClient, this.keyEncryptionKeyResolver, this.keyEncryptionKeyResolverName);
    }
}
