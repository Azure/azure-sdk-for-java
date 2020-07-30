// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.guava25.base.Preconditions;

import java.time.Duration;

public class EncryptionKeyUnwrapResult {

    /**
     *  Initializes a new instance of the result of unwrapping a wrapped data encryption key.
     *
     * @param dataEncryptionKey Raw form of data encryption key.
     *                          The byte array passed in must not be modified after this call by the {@link EncryptionKeyWrapProvider}
     * @param clientCacheTimeToLive  Amount of time after which the raw data encryption key must not be used
     *                               without invoking the  {@link EncryptionKeyWrapProvider#unwrapKey(byte[], EncryptionKeyWrapMetadata)}
     *
     */
    public EncryptionKeyUnwrapResult(byte[] dataEncryptionKey, Duration clientCacheTimeToLive) {
        Preconditions.checkNotNull(dataEncryptionKey, "dataEncryptionKey is null");
        this.dataEncryptionKey = dataEncryptionKey;
        this.clientCacheTimeToLive = clientCacheTimeToLive;
    }

    /**
     * Gets raw form of the data encryption key.
     * @return encrypted key.
     */
    public byte[] getDataEncryptionKey() {
        return dataEncryptionKey;
    }

    /**
     * Gets amount of time after which the raw data encryption key must not be used
     * without invoking the {@link EncryptionKeyWrapProvider#unwrapKey(byte[], EncryptionKeyWrapMetadata)}
     * @return client cache time to live.
     */
    public Duration getClientCacheTimeToLive() {
        return clientCacheTimeToLive;
    }

    private final byte[] dataEncryptionKey;
    private final Duration clientCacheTimeToLive;
}
