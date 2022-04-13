// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.CosmosAsyncClientEncryptionKey;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.encryption.implementation.keyprovider.EncryptionKeyStoreProviderImpl;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.DataEncryptionKeyAlgorithm;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.KeyEncryptionKey;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.MicrosoftDataEncryptionException;
import com.azure.cosmos.encryption.implementation.mdesrc.cryptography.ProtectedDataEncryptionKey;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosClientEncryptionKeyProperties;
import com.azure.cosmos.models.CosmosClientEncryptionKeyResponse;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.azure.cosmos.util.CosmosPagedFlux;
import reactor.core.publisher.Mono;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * CosmosEncryptionAsyncDatabase with encryption capabilities.
 */
public final class CosmosEncryptionAsyncDatabase {
    private final CosmosAsyncDatabase cosmosAsyncDatabase;
    private final CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient;
    private final static ImplementationBridgeHelpers.CosmosAsyncClientEncryptionKeyHelper.CosmosAsyncClientEncryptionKeyAccessor cosmosAsyncClientEncryptionKeyAccessor = ImplementationBridgeHelpers.CosmosAsyncClientEncryptionKeyHelper.getCosmosAsyncClientEncryptionKeyAccessor();

    CosmosEncryptionAsyncDatabase(CosmosAsyncDatabase cosmosAsyncDatabase,
                                  CosmosEncryptionAsyncClient cosmosEncryptionAsyncClient) {
        this.cosmosAsyncDatabase = cosmosAsyncDatabase;
        this.cosmosEncryptionAsyncClient = cosmosEncryptionAsyncClient;
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
             throw new IllegalArgumentException("clientEncryptionKeyId is null or empty");
        }

        if (StringUtils.isEmpty(encryptionAlgorithm)) {
            throw new IllegalArgumentException("encryptionAlgorithm is null or empty");
        }

        if (StringUtils.isEmpty(encryptionKeyWrapMetadata.getAlgorithm())) {
            throw new IllegalArgumentException("Key Encryption Key Algorithm in EncryptionKeyWrapMetadata is null or empty");
        }

        if (!encryptionAlgorithm.equals(DataEncryptionKeyAlgorithm.AEAD_AES_256_CBC_HMAC_SHA256.toString())) {
            throw new IllegalArgumentException(String.format("Invalid Data Encryption Algorithm '%s'", encryptionAlgorithm));
        }

        if (!encryptionKeyWrapMetadata.getAlgorithm().equals(EncryptionKeyStoreProviderImpl.RSA_OAEP)) {
            throw new IllegalArgumentException(String.format("Invalid Key Encryption Key Algorithm in EncryptionKeyWrapMetadata '%s'", encryptionKeyWrapMetadata.getAlgorithm()));
        }

        if (!this.cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolverName().equals(encryptionKeyWrapMetadata.getType())) {
            throw new IllegalArgumentException("The EncryptionKeyWrapMetadata Type value does not match with the " +
                "keyEncryptionKeyResolverName configured on the Client. Please refer to https://aka" +
                ".ms/CosmosClientEncryption for more details.");
        }

        try {
            KeyEncryptionKey keyEncryptionKey = KeyEncryptionKey.getOrCreate(encryptionKeyWrapMetadata.getName(),
                encryptionKeyWrapMetadata.getValue(), this.cosmosEncryptionAsyncClient.getEncryptionKeyStoreProviderImpl(), false);
            ProtectedDataEncryptionKey protectedDataEncryptionKey =
                new ProtectedDataEncryptionKey(clientEncryptionKeyId, keyEncryptionKey);
            byte[] wrappedDataEncryptionKey = protectedDataEncryptionKey.getEncryptedValue();
            CosmosClientEncryptionKeyProperties clientEncryptionKeyProperties =
                new CosmosClientEncryptionKeyProperties(clientEncryptionKeyId, encryptionAlgorithm,
                    wrappedDataEncryptionKey, encryptionKeyWrapMetadata);
            return this.cosmosAsyncDatabase.createClientEncryptionKey(clientEncryptionKeyProperties);
        } catch (NoSuchAlgorithmException | MicrosoftDataEncryptionException | InvalidKeyException ex) {
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
            throw new IllegalArgumentException("clientEncryptionKeyId is null or empty");
        }

        if (StringUtils.isEmpty(newEncryptionKeyWrapMetadata.getAlgorithm())) {
            throw new IllegalArgumentException("Key Encryption Key Algorithm in EncryptionKeyWrapMetadata is null or " +
                "empty");
        }

        if (!this.cosmosEncryptionAsyncClient.getKeyEncryptionKeyResolverName().equals(newEncryptionKeyWrapMetadata.getType())) {
            throw new IllegalArgumentException("The EncryptionKeyWrapMetadata Type value does not match with the " +
                "keyEncryptionKeyResolverName configured on the Client. Please refer to https://aka" +
                ".ms/CosmosClientEncryption for more details.");
        }

        if (!newEncryptionKeyWrapMetadata.getAlgorithm().equals(EncryptionKeyStoreProviderImpl.RSA_OAEP)) {
            throw new IllegalArgumentException(String.format("Invalid Key Encryption Key Algorithm in EncryptionKeyWrapMetadata '%s'", newEncryptionKeyWrapMetadata.getAlgorithm()));
        }

        try {
            CosmosAsyncClientEncryptionKey clientEncryptionKey =
                this.cosmosAsyncDatabase.getClientEncryptionKey(clientEncryptionKeyId);
            return cosmosAsyncClientEncryptionKeyAccessor.readClientEncryptionKey(clientEncryptionKey, null).flatMap(cosmosClientEncryptionKeyResponse -> {
                CosmosClientEncryptionKeyProperties clientEncryptionKeyProperties =
                    cosmosClientEncryptionKeyResponse.getProperties();
                try {
                    KeyEncryptionKey keyEncryptionKey =
                        KeyEncryptionKey.getOrCreate(clientEncryptionKeyProperties.getEncryptionKeyWrapMetadata().getName(),
                            clientEncryptionKeyProperties.getEncryptionKeyWrapMetadata().getValue(),
                            this.cosmosEncryptionAsyncClient.getEncryptionKeyStoreProviderImpl(), false);
                    byte[] unwrappedKey =
                        keyEncryptionKey.decryptEncryptionKey(clientEncryptionKeyProperties.getWrappedDataEncryptionKey());
                    keyEncryptionKey = KeyEncryptionKey.getOrCreate(newEncryptionKeyWrapMetadata.getName(),
                        newEncryptionKeyWrapMetadata.getValue(), this.cosmosEncryptionAsyncClient.getEncryptionKeyStoreProviderImpl(), false);
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
    public CosmosEncryptionAsyncContainer getCosmosEncryptionAsyncContainer(CosmosAsyncContainer container) {
        return new CosmosEncryptionAsyncContainer(container, this.cosmosEncryptionAsyncClient);
    }

    /**
     * Gets a Container with Encryption capabilities
     *
     * @param containerId original container id
     * @return container with encryption capabilities
     */
    public CosmosEncryptionAsyncContainer getCosmosEncryptionAsyncContainer(String containerId) {
        CosmosAsyncContainer cosmosAsyncContainer = this.cosmosAsyncDatabase.getContainer(containerId);
        return new CosmosEncryptionAsyncContainer(cosmosAsyncContainer, this.cosmosEncryptionAsyncClient);
    }

    /**
     * Gets the CosmosEncryptionAsyncClient.
     * @return cosmosEncryptionAsyncClient
     */
    CosmosEncryptionAsyncClient getCosmosEncryptionAsyncClient() {
        return cosmosEncryptionAsyncClient;
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
