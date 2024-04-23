// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;

/**
 * Represents a database client encryption key in the Azure Cosmos DB database service.
 */
public final class ClientEncryptionKey extends Resource {
    private String encryptionAlgorithm;
    private byte[] wrappedDataEncryptionKey;
    private EncryptionKeyWrapMetadata encryptionKeyWrapMetadata;

    /**
     * Initialize a ClientEncryptionKey object.
     */
    public ClientEncryptionKey() {
        super();
    }

    /**
     * Initialize a ClientEncryptionKey object from json string.
     *
     * @param jsonNode the json node that represents the database clientEncryptionKey.
     */
    public ClientEncryptionKey(ObjectNode jsonNode) {
        super(jsonNode);
    }

    public String getEncryptionAlgorithm() {
        if (this.encryptionAlgorithm == null) {
            if (super.has(Constants.Properties.ENCRYPTION_ALGORITHM)) {
                this.encryptionAlgorithm = super.getString(Constants.Properties.ENCRYPTION_ALGORITHM);
            }
        }
        return this.encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.set(
            Constants.Properties.ENCRYPTION_ALGORITHM,
            encryptionAlgorithm,
            CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    public byte[] getWrappedDataEncryptionKey() {
        if (this.wrappedDataEncryptionKey == null) {
            if (super.has(Constants.Properties.WRAPPED_DATA_ENCRYPTION_KEY)) {
                this.wrappedDataEncryptionKey = super.getObject(Constants.Properties.WRAPPED_DATA_ENCRYPTION_KEY,
                    byte[].class);
            }
        }
        return this.wrappedDataEncryptionKey;
    }

    public void setWrappedDataEncryptionKey(byte[] wrappedDataEncryptionKey) {
        this.wrappedDataEncryptionKey = wrappedDataEncryptionKey;
        this.set(
            Constants.Properties.WRAPPED_DATA_ENCRYPTION_KEY,
            this.wrappedDataEncryptionKey,
            CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    public EncryptionKeyWrapMetadata getEncryptionKeyWrapMetadata() {
        if (this.encryptionKeyWrapMetadata == null) {
            if (super.has(Constants.Properties.KEY_WRAP_METADATA)) {
                this.encryptionKeyWrapMetadata = super.getObject(Constants.Properties.KEY_WRAP_METADATA,
                    EncryptionKeyWrapMetadata.class);
            }
        }
        return this.encryptionKeyWrapMetadata;
    }

    public void setEncryptionKeyWrapMetadata(EncryptionKeyWrapMetadata encryptionKeyWrapMetadata) {
        this.encryptionKeyWrapMetadata = encryptionKeyWrapMetadata;
        this.set(
            Constants.Properties.KEY_WRAP_METADATA,
            this.encryptionKeyWrapMetadata,
            CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !ClientEncryptionKey.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        ClientEncryptionKey typedObj = (ClientEncryptionKey) obj;
        return (typedObj.getResourceId().equals(this.getResourceId()) &&
            Arrays.equals(typedObj.getWrappedDataEncryptionKey(), this.getWrappedDataEncryptionKey()) &&
            typedObj.getEncryptionKeyWrapMetadata().equals(this.getEncryptionKeyWrapMetadata()));
    }

    @Override
    public int hashCode() {
        return this.getResourceId().hashCode();
    }
}
