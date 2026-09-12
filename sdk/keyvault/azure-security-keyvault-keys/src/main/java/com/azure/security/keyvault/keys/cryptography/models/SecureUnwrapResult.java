// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

/**
 * Represents the details of a secure key unwrap operation result.
 */
@Immutable
public final class SecureUnwrapResult {
    /**
     * The unwrapped key content.
     */
    private final byte[] key;

    /**
     * The algorithm used for the secure key unwrap operation.
     */
    private final SecureKeyWrapAlgorithm algorithm;

    /**
     * The identifier of the key used for the secure key unwrap operation.
     */
    private final String keyId;

    /**
     * Creates the instance of {@link SecureUnwrapResult} holding the unwrapped key content.
     *
     * @param key The unwrapped key content.
     * @param algorithm The algorithm used for the operation.
     * @param keyId The identifier of the key used for the operation.
     */
    public SecureUnwrapResult(byte[] key, SecureKeyWrapAlgorithm algorithm, String keyId) {
        this.key = CoreUtils.clone(key);
        this.algorithm = algorithm;
        this.keyId = keyId;
    }

    /**
     * Get the unwrapped key content.
     *
     * @return The unwrapped key content.
     */
    public byte[] getKey() {
        return CoreUtils.clone(key);
    }

    /**
     * Get the algorithm used for the secure key unwrap operation.
     *
     * @return The secure key wrap algorithm used.
     */
    public SecureKeyWrapAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the identifier of the key used for the secure key unwrap operation.
     *
     * @return The key identifier.
     */
    public String getKeyId() {
        return keyId;
    }
}
