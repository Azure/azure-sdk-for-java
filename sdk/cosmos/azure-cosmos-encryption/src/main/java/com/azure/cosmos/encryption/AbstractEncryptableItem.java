// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.ItemDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Input type should implement this abstract class for lazy decryption and to retrieve the details in the write path.
 */
abstract class AbstractEncryptableItem {

    DecryptableItem decryptableItem = null;

    /**
     * Gets DecryptableItem
     *
     * @return DecryptableItem.
     */
    public abstract DecryptableItem getDecryptableItem();

    /**
     * Gets the input payload in byte[] format.
     *
     * @param serializer Cosmos Serializer
     * @return Input payload in stream format.
     */
    protected abstract byte[] toStream(ItemDeserializer serializer);

    /**
     * Populates the DecryptableItem that can be used getting the decryption result.
     *
     * @param decryptableContent The encrypted content which is yet to be decrypted.
     * @param encryptor Encryptor instance which will be used for decryption.
     * @param cosmosSerializer serializer instance which will be used for deserializing the content after decryption
     */
    protected void setDecryptableItem(
        ObjectNode decryptableContent,
        Encryptor encryptor,
        ItemDeserializer cosmosSerializer) {
        if (this.decryptableItem != null) {
            throw new IllegalStateException("already initialized");
        }

        this.decryptableItem = new DecryptableItemCore(
            decryptableContent,
            encryptor,
            cosmosSerializer);
    }
}
