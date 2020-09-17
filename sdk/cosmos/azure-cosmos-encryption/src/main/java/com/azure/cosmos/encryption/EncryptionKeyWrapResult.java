// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.guava25.base.Preconditions;


/**
 * Result from a {@link EncryptionKeyWrapProvider} on wrapping a data encryption key.
 */
public class EncryptionKeyWrapResult {

    private final byte[] wrappedDataEncryptionKey;
    private final EncryptionKeyWrapMetadata encryptionKeyWrapMetadata;

    /**
     * Initializes a new instance of the result of wrapping a data encryption key.
     *
     * @param wrappedDataEncryptionKey  Wrapped form of data encryption key.
     *                                  The byte array passed in must not be modified after this call by the {@link EncryptionKeyWrapResult}
     * @param encryptionKeyWrapMetadata Metadata that can be used by the wrap provider to unwrap the data encryption key.
     */
    public EncryptionKeyWrapResult(byte[] wrappedDataEncryptionKey, EncryptionKeyWrapMetadata encryptionKeyWrapMetadata) {
        Preconditions.checkNotNull(wrappedDataEncryptionKey, "wrappedDataEncryptionKey is null");
        Preconditions.checkNotNull(encryptionKeyWrapMetadata, "encryptionKeyWrapMetadata is null");

        this.wrappedDataEncryptionKey = wrappedDataEncryptionKey;
        this.encryptionKeyWrapMetadata = encryptionKeyWrapMetadata;
    }

    /**
     * Gets wrapped form of the data encryption key.
     * @return wrapped data encryption key.
     */
    public byte[] getWrappedDataEncryptionKey() {
        return wrappedDataEncryptionKey;
    }

    /**
     * Gets metadata that can be used by the wrap provider to unwrap the key.
     * @return encryption key wrap metadata.
     */
    public EncryptionKeyWrapMetadata getEncryptionKeyWrapMetadata() {
        return encryptionKeyWrapMetadata;
    }
}
