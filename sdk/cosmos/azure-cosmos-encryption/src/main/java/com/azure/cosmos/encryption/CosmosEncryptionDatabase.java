// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosClientEncryptionKey;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.util.CosmosPagedIterable;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * CosmosEncryptionDatabase with encryption capabilities.
 */
public class CosmosEncryptionDatabase {
    private final CosmosDatabase cosmosDatabase;
    private final CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase;

    CosmosEncryptionDatabase(CosmosDatabase cosmosDatabase,
                             CosmosEncryptionAsyncDatabase cosmosEncryptionAsyncDatabase) {
        this.cosmosDatabase = cosmosDatabase;
        this.cosmosEncryptionAsyncDatabase = cosmosEncryptionAsyncDatabase;
    }

    /**
     * Gets a CosmosClientEncryptionKey object without making a service call
     *
     * @param id id of the clientEncryptionKey
     * @return Cosmos ClientEncryptionKey
     */
    public CosmosClientEncryptionKey getClientEncryptionKey(String id) {
        return this.cosmosDatabase.getClientEncryptionKey(id);
    }

    /**
     * Reads all cosmos client encryption keys in a database.
     * <p>
     * After subscription the operation will be performed. The {@link CosmosPagedIterable} will
     * contain one or several feed response of the read cosmos client encryption keys. In case of
     * failure the {@link CosmosPagedIterable} will error.
     *
     * @return a {@link CosmosPagedIterable}.
     */
    public CosmosPagedIterable<CosmosClientEncryptionKeyProperties> readAllClientEncryptionKeys() {
        return this.cosmosDatabase.readAllClientEncryptionKeys();
    }

    /**
     * Creates a client encryption key after subscription the operation will be performed.
     *
     * @param clientEncryptionKeyId     Client Encryption Key id.
     * @param encryptionAlgorithm       Encryption Algorithm.
     * @param encryptionKeyWrapMetadata EncryptionKeyWrapMetadata.
     * @return the resource response with the
     * created cosmos client encryption key or an error.
     */
    public CosmosClientEncryptionKeyResponse createClientEncryptionKey(String clientEncryptionKeyId,
                                                                       String encryptionAlgorithm,
                                                                       EncryptionKeyWrapMetadata encryptionKeyWrapMetadata) {
        return blockClientEncryptionKeyResponse(this.cosmosEncryptionAsyncDatabase.createClientEncryptionKey(clientEncryptionKeyId, encryptionAlgorithm, encryptionKeyWrapMetadata));
    }

    /**
     * Rewrap a cosmos client encryption key
     *
     * @param clientEncryptionKeyId        the client encryption key properties to create.
     * @param newEncryptionKeyWrapMetadata EncryptionKeyWrapMetadata.
     * @return the resource response with the read client encryption key or an error.
     */
    public CosmosClientEncryptionKeyResponse rewrapClientEncryptionKey(String clientEncryptionKeyId,
                                                                       EncryptionKeyWrapMetadata newEncryptionKeyWrapMetadata) {

        return blockClientEncryptionKeyResponse(this.cosmosEncryptionAsyncDatabase.rewrapClientEncryptionKey(clientEncryptionKeyId, newEncryptionKeyWrapMetadata));
    }

    /**
     * Gets a CosmosDatabase.
     *
     * @return cosmos database
     */
    public CosmosDatabase getCosmosDatabase() {
        return cosmosDatabase;
    }

    /**
     * Gets a Container with Encryption capabilities
     *
     * @param container original container
     * @return container with encryption capabilities
     */
    public CosmosEncryptionContainer getCosmosEncryptionContainer(CosmosContainer container) {
        CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer =
            this.cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(container.getId());
        return new CosmosEncryptionContainer(container, cosmosEncryptionAsyncContainer);
    }

    /**
     * Gets a Container with Encryption capabilities
     *
     * @param containerId original container id
     * @return container with encryption capabilities
     */
    public CosmosEncryptionContainer getCosmosEncryptionAsyncContainer(String containerId) {
        CosmosEncryptionAsyncContainer cosmosEncryptionAsyncContainer =
            this.cosmosEncryptionAsyncDatabase.getCosmosEncryptionAsyncContainer(containerId);
        CosmosContainer cosmosContainer = this.cosmosDatabase.getContainer(containerId);
        return new CosmosEncryptionContainer(cosmosContainer, cosmosEncryptionAsyncContainer);
    }

    /**
     * Block cosmos clientEncryptionKey response
     *
     * @param cosmosClientEncryptionKeyResponseMono the clientEncryptionKey mono.
     * @return the cosmos clientEncryptionKey response.
     */
    private CosmosClientEncryptionKeyResponse blockClientEncryptionKeyResponse(Mono<CosmosClientEncryptionKeyResponse> cosmosClientEncryptionKeyResponseMono) {
        try {
            return cosmosClientEncryptionKeyResponseMono.block();
        } catch (Exception ex) {
            if (ex instanceof CosmosException) {
                throw (CosmosException) ex;
            } else {
                throw ex;
            }
        }
    }
}
