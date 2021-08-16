// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.models.EncryptionKeyWrapMetadata;

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
     * @param jsonString the json string that represents the database clientEncryptionKey.
     */
    public ClientEncryptionKey(String jsonString) {
        super(jsonString);
    }

    public String getEncryptionAlgorithm() {
        if (this.encryptionAlgorithm == null) {
            if (super.has(Constants.Properties.ENCRYPTION_ALGORITHM_ID)) {
                this.encryptionAlgorithm = super.getString(Constants.Properties.ENCRYPTION_ALGORITHM_ID);
            }
        }
        return this.encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        BridgeInternal.setProperty(this, Constants.Properties.ENCRYPTION_ALGORITHM_ID,
            encryptionAlgorithm);
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
        BridgeInternal.setProperty(this, Constants.Properties.WRAPPED_DATA_ENCRYPTION_KEY,
            this.wrappedDataEncryptionKey);
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
        BridgeInternal.setProperty(this, Constants.Properties.KEY_WRAP_METADATA,
            this.encryptionKeyWrapMetadata);
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
