// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.EncryptionContainerResponse;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.microsoft.data.encryption.cryptography.EncryptionKeyStoreProvider;
import com.microsoft.data.encryption.cryptography.KeyEncryptionKey;
import com.microsoft.data.encryption.cryptography.ProtectedDataEncryptionKey;
import reactor.core.publisher.Mono;

/**
 * EncryptionCosmosAsyncDatabase with encryption capabilities.
 */
public class EncryptionCosmosAsyncDatabase {

    private final CosmosAsyncDatabase cosmosAsyncDatabase;

    private EncryptionCosmosAsyncClient encryptionCosmosAsyncClient;

    public EncryptionCosmosAsyncDatabase(CosmosAsyncDatabase cosmosAsyncDatabase,
                                         EncryptionCosmosAsyncClient encryptionCosmosAsyncClient) {
        this.cosmosAsyncDatabase = cosmosAsyncDatabase;
        this.encryptionCosmosAsyncClient = encryptionCosmosAsyncClient;
    }

    /**
     * Creates a encrypted container.
     *
     * @param containerProperties  the container properties.
     * @param throughputProperties the throughput properties.
     * @param options              the request options.
     * @return the mono.
     */
    public Mono<EncryptionContainerResponse> createEncryptionContainerAsync(CosmosContainerProperties containerProperties,
                                                                            ThroughputProperties throughputProperties,
                                                                            CosmosContainerRequestOptions options) {
        return this.cosmosAsyncDatabase.createContainer(containerProperties, throughputProperties,
            options).map(cosmosContainerResponse -> {
            EncryptionContainerResponse encryptionContainerResponse =
                new EncryptionContainerResponse(cosmosContainerResponse,
                    new EncryptionCosmosAsyncContainer(cosmosAsyncDatabase.getContainer(cosmosContainerResponse.getProperties().getId()), this.encryptionCosmosAsyncClient));
            return encryptionContainerResponse;
        });
    }

    /**
     * Creates a encrypted cosmos container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties the containerProperties.
     * @param options             the cosmos container request options.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     * @throws IllegalArgumentException containerProperties can not be null.
     */
    public Mono<EncryptionContainerResponse> createEncryptionContainerAsync(CosmosContainerProperties containerProperties,
                                                                            CosmosContainerRequestOptions options) {
        return this.cosmosAsyncDatabase.createContainer(containerProperties,
            options).map(cosmosContainerResponse -> {
            EncryptionContainerResponse encryptionContainerResponse =
                new EncryptionContainerResponse(cosmosContainerResponse,
                    new EncryptionCosmosAsyncContainer(cosmosAsyncDatabase.getContainer(cosmosContainerResponse.getProperties().getId()), this.encryptionCosmosAsyncClient));
            return encryptionContainerResponse;
        });
    }

    /**
     * Creates a encrypted cosmos container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id                   the cosmos container id.
     * @param partitionKeyPath     the partition key path.
     * @param throughputProperties the throughput properties for the container.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<EncryptionContainerResponse> createEncryptionContainerAsync(String id,
                                                                            String partitionKeyPath,
                                                                            ThroughputProperties throughputProperties) {
        return this.cosmosAsyncDatabase.createContainer(id, partitionKeyPath,
            throughputProperties).map(cosmosContainerResponse -> {
            EncryptionContainerResponse encryptionContainerResponse =
                new EncryptionContainerResponse(cosmosContainerResponse,
                    new EncryptionCosmosAsyncContainer(cosmosAsyncDatabase.getContainer(cosmosContainerResponse.getProperties().getId()), this.encryptionCosmosAsyncClient));
            return encryptionContainerResponse;
        });
    }

    /**
     * Creates a encrypted cosmos container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id               the cosmos container id.
     * @param partitionKeyPath the partition key path.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<EncryptionContainerResponse> createEncryptionContainerAsync(String id,
                                                                            String partitionKeyPath) {
        return this.cosmosAsyncDatabase.createContainer(id, partitionKeyPath).map(cosmosContainerResponse -> {
            EncryptionContainerResponse encryptionContainerResponse =
                new EncryptionContainerResponse(cosmosContainerResponse,
                    new EncryptionCosmosAsyncContainer(cosmosAsyncDatabase.getContainer(cosmosContainerResponse.getProperties().getId()), this.encryptionCosmosAsyncClient));
            return encryptionContainerResponse;
        });
    }

    /**
     * Creates a encrypted cosmos container.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties the container properties.
     * @return a {@link Mono} containing the single cosmos container response with
     * the created container or an error.
     * @throws IllegalArgumentException containerProperties cannot be null.
     */
    public Mono<EncryptionContainerResponse> createEncryptionContainerAsync(CosmosContainerProperties containerProperties) {
        return this.cosmosAsyncDatabase.createContainer(containerProperties).map(cosmosContainerResponse -> {
            EncryptionContainerResponse encryptionContainerResponse =
                new EncryptionContainerResponse(cosmosContainerResponse,
                    new EncryptionCosmosAsyncContainer(cosmosAsyncDatabase.getContainer(cosmosContainerResponse.getProperties().getId()), this.encryptionCosmosAsyncClient));
            return encryptionContainerResponse;
        });
    }

    /**
     * Creates a encrypted cosmos container with custom throughput properties.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param containerProperties  the container properties.
     * @param throughputProperties the throughput properties for the container.
     * @return a {@link Mono} containing the single cosmos container response with
     * the created container or an error.
     * @throws IllegalArgumentException thown if containerProerties are null.
     */
    public Mono<EncryptionContainerResponse> createEncryptionContainerAsync(CosmosContainerProperties containerProperties,
                                                                            ThroughputProperties throughputProperties) {
        return this.cosmosAsyncDatabase.createContainer(containerProperties, throughputProperties).map(cosmosContainerResponse -> {
            EncryptionContainerResponse encryptionContainerResponse =
                new EncryptionContainerResponse(cosmosContainerResponse,
                    new EncryptionCosmosAsyncContainer(cosmosAsyncDatabase.getContainer(cosmosContainerResponse.getProperties().getId()), this.encryptionCosmosAsyncClient));
            return encryptionContainerResponse;
        });
    }

    /**
     * Creates a encrypted cosmos container if it does not exist on the service.
     * <p>
     * The throughput properties will only be used if the specified container
     * does not exist and therefor a new container will be created.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created or existing container. In case of failure the {@link Mono} will
     * error.
     *
     * @param containerProperties  the container properties.
     * @param throughputProperties the throughput properties for the container.
     * @return a {@link Mono} containing the cosmos container response with the
     * created or existing container or an error.
     */
    public Mono<EncryptionContainerResponse> createEncryptionContainerIfNotExistAsync(CosmosContainerProperties containerProperties,
                                                                                      ThroughputProperties throughputProperties) {
        return this.cosmosAsyncDatabase.createContainerIfNotExists(containerProperties, throughputProperties).map(cosmosContainerResponse -> {
            EncryptionContainerResponse encryptionContainerResponse =
                new EncryptionContainerResponse(cosmosContainerResponse,
                    new EncryptionCosmosAsyncContainer(cosmosAsyncDatabase.getContainer(cosmosContainerResponse.getProperties().getId()), this.encryptionCosmosAsyncClient));
            return encryptionContainerResponse;
        });
    }

    /**
     * Creates a encrypted cosmos container if it does not exist on the service.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id               the cosmos container id.
     * @param partitionKeyPath the partition key path.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<EncryptionContainerResponse> createEncryptionContainerIfNotExistAsync(String id,
                                                                                      String partitionKeyPath) {
        return this.cosmosAsyncDatabase.createContainerIfNotExists(id, partitionKeyPath).map(cosmosContainerResponse -> {
            EncryptionContainerResponse encryptionContainerResponse =
                new EncryptionContainerResponse(cosmosContainerResponse,
                    new EncryptionCosmosAsyncContainer(cosmosAsyncDatabase.getContainer(cosmosContainerResponse.getProperties().getId()), this.encryptionCosmosAsyncClient));
            return encryptionContainerResponse;
        });
    }

    /**
     * Creates a encrypted cosmos container if it does not exist on the service.
     * <p>
     * The throughput properties will only be used if the specified container
     * does not exist and therefor a new container will be created.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a cosmos container response with the
     * created container. In case of failure the {@link Mono} will error.
     *
     * @param id                   the cosmos container id.
     * @param partitionKeyPath     the partition key path.
     * @param throughputProperties the throughput properties for the container.
     * @return a {@link Mono} containing the cosmos container response with the
     * created container or an error.
     */
    public Mono<EncryptionContainerResponse> createEncryptionContainerIfNotExistAsync(String id,
                                                                                      String partitionKeyPath,
                                                                                      ThroughputProperties throughputProperties) {
        return this.cosmosAsyncDatabase.createContainerIfNotExists(id, partitionKeyPath, throughputProperties).map(cosmosContainerResponse -> {
            EncryptionContainerResponse encryptionContainerResponse =
                new EncryptionContainerResponse(cosmosContainerResponse,
                    new EncryptionCosmosAsyncContainer(cosmosAsyncDatabase.getContainer(cosmosContainerResponse.getProperties().getId()), this.encryptionCosmosAsyncClient));
            return encryptionContainerResponse;
        });
    }

    /**
     * Gets a CosmosAsyncClientEncryptionKey object without making a service call
     *
     * @param id id of the clientEncryptionKey
     * @return Cosmos ClientEncryptionKey
     */
    public CosmosAsyncClientEncryptionKey getClientEncryptionKey(String id) {
        return this.cosmosAsyncDatabase.getClientEncryptionKey(id);
    }

    /**
     * Reads all cosmos client encryption keys in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedFlux} will
     * contain one or several feed response of the read cosmos client encryption keys. In case of
     * failure the {@link CosmosPagedFlux} will error.
     *
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the
     * read cosmos client encryption keys or an error.
     */
    public CosmosPagedFlux<CosmosClientEncryptionKeyProperties> readAllClientEncryptionKeys() {
        return this.cosmosAsyncDatabase.readAllClientEncryptionKeys();
    }

    /**
     * Creates a client encryption key after subscription the operation will be performed. The
     * {@link Mono} upon successful completion will contain a single resource
     * response with the created client encryption key. In case of failure the {@link Mono} will
     * error.
     *
     * @param clientEncryptionKeyId     Client Encryption Key id.
     * @param encryptionAlgorithm       Encryption Algorithm.
     * @param encryptionKeyWrapMetadata EncryptionKeyWrapMetadata.
     * @return an {@link Mono} containing the single resource response with the
     * created cosmos client encryption key or an error.
     */
    public Mono<CosmosClientEncryptionKeyResponse> createClientEncryptionKey(String clientEncryptionKeyId,
                                                                             String encryptionAlgorithm,
                                                                             EncryptionKeyWrapMetadata encryptionKeyWrapMetadata) {
        if (StringUtils.isEmpty(clientEncryptionKeyId)) {
            return Mono.error(new IllegalArgumentException("clientEncryptionKeyId is null or empty"));
        }

        if (StringUtils.isEmpty(encryptionAlgorithm)) {
            return Mono.error(new IllegalArgumentException("encryptionAlgorithm is null or empty"));
        }

        EncryptionKeyStoreProvider encryptionKeyStoreProvider =
            this.encryptionCosmosAsyncClient.getEncryptionKeyStoreProvider();
        try {
            KeyEncryptionKey keyEncryptionKey = KeyEncryptionKey.getOrCreate(encryptionKeyWrapMetadata.name,
                encryptionKeyWrapMetadata.value, encryptionKeyStoreProvider, false);
            ProtectedDataEncryptionKey protectedDataEncryptionKey =
                new ProtectedDataEncryptionKey(clientEncryptionKeyId, keyEncryptionKey);
            byte[] wrappedDataEncryptionKey = protectedDataEncryptionKey.getEncryptedValue();
            CosmosClientEncryptionKeyProperties clientEncryptionKeyProperties =
                new CosmosClientEncryptionKeyProperties(clientEncryptionKeyId, encryptionAlgorithm,
                    wrappedDataEncryptionKey, encryptionKeyWrapMetadata);
            return this.cosmosAsyncDatabase.createClientEncryptionKey(clientEncryptionKeyProperties);
        } catch (Exception ex) {
            return Mono.error(ex);
        }
    }

    /**
     * Rewrap a cosmos client encryption key
     *
     * @param clientEncryptionKeyId        the client encryption key properties to create.
     * @param newEncryptionKeyWrapMetadata EncryptionKeyWrapMetadata.
     * @return a {@link Mono} containing the single resource response with the read client encryption key or an error.
     */
    public Mono<CosmosClientEncryptionKeyResponse> rewrapClientEncryptionKey(String clientEncryptionKeyId,
                                                                             EncryptionKeyWrapMetadata newEncryptionKeyWrapMetadata) {
        if (StringUtils.isEmpty(clientEncryptionKeyId)) {
            return Mono.error(new IllegalArgumentException("clientEncryptionKeyId is null or empty"));
        }

        EncryptionKeyStoreProvider encryptionKeyStoreProvider =
            this.encryptionCosmosAsyncClient.getEncryptionKeyStoreProvider();
        try {
            CosmosAsyncClientEncryptionKey clientEncryptionKey =
                this.cosmosAsyncDatabase.getClientEncryptionKey(clientEncryptionKeyId);
            return clientEncryptionKey.read().flatMap(cosmosClientEncryptionKeyResponse -> {
                CosmosClientEncryptionKeyProperties clientEncryptionKeyProperties =
                    cosmosClientEncryptionKeyResponse.getProperties();
                try {
                    KeyEncryptionKey keyEncryptionKey =
                        KeyEncryptionKey.getOrCreate(clientEncryptionKeyProperties.getEncryptionKeyWrapMetadata().name,
                            clientEncryptionKeyProperties.getEncryptionKeyWrapMetadata().value,
                            encryptionKeyStoreProvider, false);
                    byte[] unwrappedKey =
                        keyEncryptionKey.decryptEncryptionKey(clientEncryptionKeyProperties.getWrappedDataEncryptionKey());
                    keyEncryptionKey = KeyEncryptionKey.getOrCreate(newEncryptionKeyWrapMetadata.name,
                        newEncryptionKeyWrapMetadata.value, encryptionKeyStoreProvider, false);
                    byte[] rewrappedKey = keyEncryptionKey.encryptEncryptionKey(unwrappedKey);
                    clientEncryptionKeyProperties = new CosmosClientEncryptionKeyProperties(clientEncryptionKeyId,
                        clientEncryptionKeyProperties.getEncryptionAlgorithm(),
                        rewrappedKey, newEncryptionKeyWrapMetadata);
                    return clientEncryptionKey.replace(clientEncryptionKeyProperties);
                } catch (Exception ex) {
                    return Mono.error(ex);
                }

            });

        } catch (Exception ex) {
            return Mono.error(ex);
        }
    }

    /**
     * Gets a Container with Encryption capabilities
     *
     * @param container original container
     * @return container with encryption capabilities
     */
    public EncryptionCosmosAsyncContainer getEncryptedCosmosAsyncContainer(CosmosAsyncContainer container) {
        return new EncryptionCosmosAsyncContainer(container, this.encryptionCosmosAsyncClient);
    }

    /**
     * Gets a Container with Encryption capabilities
     *
     * @param containerId original container id
     * @return container with encryption capabilities
     */
    public EncryptionCosmosAsyncContainer getEncryptedCosmosAsyncContainer(String containerId) {
        CosmosAsyncContainer cosmosAsyncContainer = this.cosmosAsyncDatabase.getContainer(containerId);
        return new EncryptionCosmosAsyncContainer(cosmosAsyncContainer, this.encryptionCosmosAsyncClient);
    }

    EncryptionCosmosAsyncClient getEncryptionCosmosAsyncClient() {
        return encryptionCosmosAsyncClient;
    }

    void setEncryptionCosmosAsyncClient(EncryptionCosmosAsyncClient encryptionCosmosAsyncClient) {
        this.encryptionCosmosAsyncClient = encryptionCosmosAsyncClient;
    }

    /**
     * Gets a regular async database object.
     *
     * @return regular async database object
     */
    public CosmosAsyncDatabase getCosmosAsyncDatabase() {
        return this.cosmosAsyncDatabase;
    }
}
