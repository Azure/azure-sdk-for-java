// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ClientEncryptionKey;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.User;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Details of an encryption key for use with the Azure Cosmos DB service.
 */
public class CosmosClientEncryptionKeyProperties {
    private ClientEncryptionKey clientEncryptionKey;

    /**
     * Initialize a ClientEncryptionKey object from json string.
     *
     * @param jsonString the json string that represents the database clientEncryptionKey.
     */
    CosmosClientEncryptionKeyProperties(String jsonString) {
        this.clientEncryptionKey = new ClientEncryptionKey(jsonString);
    }

    CosmosClientEncryptionKeyProperties(ClientEncryptionKey clientEncryptionKey) {
        this.clientEncryptionKey = clientEncryptionKey;
    }

    public CosmosClientEncryptionKeyProperties(String id,
                                        String encryptionAlgorithm,
                                        byte[] wrappedDataEncryptionKey,
                                        EncryptionKeyWrapMetadata encryptionKeyWrapMetadata) {
        this.clientEncryptionKey = new ClientEncryptionKey();
        this.clientEncryptionKey.setId(id);
        this.clientEncryptionKey.setEncryptionAlgorithm(encryptionAlgorithm);
        this.clientEncryptionKey.setWrappedDataEncryptionKey(wrappedDataEncryptionKey);
        this.clientEncryptionKey.setEncryptionKeyWrapMetadata(encryptionKeyWrapMetadata);
    }

    public String getEncryptionAlgorithm() {
        return this.clientEncryptionKey.getEncryptionAlgorithm();
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.clientEncryptionKey.setEncryptionAlgorithm(encryptionAlgorithm);
    }

    public byte[] getWrappedDataEncryptionKey() {
        return this.clientEncryptionKey.getWrappedDataEncryptionKey();
    }

    public void setWrappedDataEncryptionKey(byte[] wrappedDataEncryptionKey) {
        this.clientEncryptionKey.setWrappedDataEncryptionKey(wrappedDataEncryptionKey);
    }

    public EncryptionKeyWrapMetadata getEncryptionKeyWrapMetadata() {
        return this.clientEncryptionKey.getEncryptionKeyWrapMetadata();
    }

    public void setEncryptionKeyWrapMetadata(EncryptionKeyWrapMetadata encryptionKeyWrapMetadata) {
        this.clientEncryptionKey.setEncryptionKeyWrapMetadata(encryptionKeyWrapMetadata);
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return this.clientEncryptionKey.getId();
    }

    /**
     * Sets the name of the resource.
     *
     * @param id the name of the resource.
     * @return the current instance of {@link CosmosContainerProperties}.
     */
    public CosmosClientEncryptionKeyProperties setId(String id) {
        this.clientEncryptionKey.setId(id);
        return this;
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    public String getResourceId() {
        return this.clientEncryptionKey.getResourceId();
    }

    /**
     * Get the last modified timestamp associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the timestamp.
     */
    public Instant getTimestamp() {
        return this.clientEncryptionKey.getTimestamp();
    }

    /**
     * Get the entity tag associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the e tag.
     */
    public String getETag() {
        return this.clientEncryptionKey.getETag();
    }

    Resource getResource() {
        return this.clientEncryptionKey;
    }

    ClientEncryptionKey getClientEncryptionKey() {
        return new ClientEncryptionKey(this.clientEncryptionKey.toJson());
    }

    static List<CosmosClientEncryptionKeyProperties> getClientEncryptionKeys(List<ClientEncryptionKey> results) {
        return results.stream().map(CosmosClientEncryptionKeyProperties::new).collect(Collectors.toList());
    }

}
