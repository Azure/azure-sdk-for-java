// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.guava25.base.Preconditions;

import java.util.List;

/**
 * Provides information about decryption operation.
 */
public final class DecryptionInfo {

    private final List<String> PathsDecrypted; // immutable
    private final String DataEncryptionKeyId;

    /**
     * Gets the list of JSON paths decrypted.
     *
     * @return list of decrypted paths.
     */
    public List<String> getPathsDecrypted() {
        return PathsDecrypted;
    }

    /**
     * Gets the DataEncryptionKey id used for decryption.
     *
     * @return DataEncryptionKey id.
     */
    public String getDataEncryptionKeyId() {
        return DataEncryptionKeyId;
    }

    /**
     * Initializes a new instance of the {@link DecryptionInfo}
     *
     * @param pathsDecrypted List of paths that were decrypted.
     * @param dataEncryptionKeyId DataEncryptionKey id used for decryption.
     */
    public DecryptionInfo(List<String> pathsDecrypted,
                          String dataEncryptionKeyId) {
        Preconditions.checkNotNull(pathsDecrypted, "pathsDecrypted");
        Preconditions.checkNotNull(dataEncryptionKeyId, "dataEncryptionKeyId");

        this.PathsDecrypted = pathsDecrypted;
        this.DataEncryptionKeyId = dataEncryptionKeyId;
    }
}
