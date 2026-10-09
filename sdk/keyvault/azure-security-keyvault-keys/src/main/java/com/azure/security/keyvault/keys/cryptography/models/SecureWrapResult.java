// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

/**
 * Represents the details of a secure key wrap operation result.
 */
@Immutable
public final class SecureWrapResult {
    /**
     * The encrypted key content.
     */
    private final byte[] encryptedKey;

    /**
     * The identifier of the key used for the secure key wrap operation.
     */
    private final String keyId;

    /**
     * The algorithm used to wrap the key content.
     */
    private final SecureKeyWrapAlgorithm algorithm;

    /**
     * Creates the instance of {@link SecureWrapResult} holding the secure key wrap operation response details.
     *
     * @param encryptedKey The encrypted key content.
     * @param algorithm The algorithm used to wrap the key content.
     * @param keyId The identifier of the key used for the secure key wrap operation.
     */
    public SecureWrapResult(byte[] encryptedKey, SecureKeyWrapAlgorithm algorithm, String keyId) {
        this.encryptedKey = CoreUtils.clone(encryptedKey);
        this.keyId = keyId;
        this.algorithm = algorithm;
    }

    /**
     * Get the encrypted key content.
     *
     * @return The encrypted key.
     */
    public byte[] getEncryptedKey() {
        return CoreUtils.clone(encryptedKey);
    }

    /**
     * Get the algorithm used to wrap the key content.
     *
     * @return The secure key wrap algorithm.
     */
    public SecureKeyWrapAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the identifier of the key used for the secure key wrap operation.
     *
     * @return The key identifier.
     */
    public String getKeyId() {
        return keyId;
    }
}
