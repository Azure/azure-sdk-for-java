// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;


import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.encryption.keyprovider.EncryptionKeyWrapProvider;

/**
 * Helper class to build {@link CosmosEncryptionAsyncClient} and {@link CosmosEncryptionClient}
 * instances as logical representation of the Azure Cosmos database service.
 * <p>
 * When building client, cosmosAsyncClient()/cosmosClient() and encryptionKeyWrapProvider() are mandatory APIs, without these the initialization will fail.
 * <pre>
 *     Building Cosmos Encryption Async Client APIs
 * {@code
 * CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient = new CosmosEncryptionClientBuilder()
 *         .cosmosAsyncClient(cosmosAsyncClient)
 *         .encryptionKeyWrapProvider(encryptionKeyWrapProvider)
 *         .buildAsyncClient();
 * }
 * </pre>
 *
 * <pre>
 *     Building Cosmos Encryption Sync Client minimal APIs
 *  * {@code
 * CosmosEncryptionClient client = new CosmosEncryptionClientBuilder()
 *         .cosmosClient(cosmosClient)
 *         .encryptionKeyWrapProvider(encryptionKeyWrapProvider)
 *         .buildClient();
 * }
 * </pre>
 */
@ServiceClientBuilder(serviceClients = {CosmosEncryptionClient.class, CosmosEncryptionAsyncClient.class})
public class CosmosEncryptionClientBuilder {

    private CosmosAsyncClient cosmosAsyncClient;
    private CosmosClient cosmosClient;
    private EncryptionKeyWrapProvider encryptionKeyWrapProvider;

    /**
     * Instantiates a new Cosmos encryption client builder.
     */
    public CosmosEncryptionClientBuilder() {
    }

    /**
     * Sets the cosmos core async client to be used.
     *
     * @param cosmosAsyncClient cosmos async client
     * @return current CosmosClientBuilder
     */
    public CosmosEncryptionClientBuilder cosmosAsyncClient(CosmosAsyncClient cosmosAsyncClient){
        this.cosmosAsyncClient = cosmosAsyncClient;
        return this;
    }

    /**
     * Sets the cosmos core sync client to be used.
     *
     * @param cosmosClient cosmos sync client
     * @return current CosmosClientBuilder
     */
    public CosmosEncryptionClientBuilder cosmosClient(CosmosClient cosmosClient){
        this.cosmosClient = cosmosClient;
        return this;
    }

    /**
     * Sets the key wrap provider
     *
     * @param encryptionKeyWrapProvider  custom provider implementation of {@link EncryptionKeyWrapProvider}
     * @return current CosmosClientBuilder
     */
    public CosmosEncryptionClientBuilder encryptionKeyWrapProvider(EncryptionKeyWrapProvider encryptionKeyWrapProvider) {
        this.encryptionKeyWrapProvider = encryptionKeyWrapProvider;
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

        if(this.encryptionKeyWrapProvider == null) {
            throw new IllegalArgumentException("EncryptionKeyWrapProvider has not been provided.");
        }

        return new CosmosEncryptionAsyncClient(this.cosmosAsyncClient, this.encryptionKeyWrapProvider);
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

        if(this.encryptionKeyWrapProvider == null) {
            throw new IllegalArgumentException("EncryptionKeyWrapProvider has not been provided.");
        }

        return new CosmosEncryptionClient(this.cosmosClient, this.encryptionKeyWrapProvider);
    }
}
