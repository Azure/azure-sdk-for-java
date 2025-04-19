// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;

import static io.clientcore.core.utils.CoreUtils.arrayCopy;

/**
 * Represents the details of key unwrap operation result.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class UnwrapResult {
    /**
     * The unwrapped key content.
     */
    private final byte[] key;

    /**
     * The algorithm used for the key wrap operation.
     */
    private final KeyWrapAlgorithm algorithm;

    /**
     * The identifier of the key used for the key wrap operation.
     */
    private final String keyId;

    /**
     * Creates the instance of KeyUnwrap Result holding the unwrapped key content.
     *
     * @param key The unwrapped key content.
     * @param algorithm The algorithm used for the operation
     * @param keyId The id of key used for the operation
     */
    public UnwrapResult(byte[] key, KeyWrapAlgorithm algorithm, String keyId) {
        this.key = arrayCopy(key);
        this.algorithm = algorithm;
        this.keyId = keyId;
    }

    /**
     * Get the unwrapped key content.
     *
     * @return The unwrapped key content.
     */
    public byte[] getKey() {
        return arrayCopy(key);
    }

    /**
     * Get the algorithm used for key wrap operation.
     *
     * @return The encryption algorithm used.
     */
    public KeyWrapAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the identifier of the key used for the key wrap encryption
     *
     * @return the key identifier
     */
    public String getKeyId() {
        return keyId;
    }
}
