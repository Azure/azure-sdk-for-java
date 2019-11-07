// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

/**
 * Represents the details of wrap operation result.
 */
@Immutable
public final class WrapResult {
    /**
     * The encrypted key content
     */
    private final byte[] encryptedKey;

    /**
     * The identifier of the key used for the encryption operation.
     */
    private final String keyId;


    /**
     * The key wrap algorithm used to wrap the key content.
     */
    private final KeyWrapAlgorithm algorithm;


    /**
     * Creates the instance of KeyWrapResult holding the key wrap operation response details.
     * @param encryptedKey The unwrapped key content.
     * @param algorithm The algorithm used to wrap the key content.
     * @param keyId The identifier of the key usd for the key wrap operation.
     */
    public WrapResult(byte[] encryptedKey, KeyWrapAlgorithm algorithm, String keyId) {
        this.encryptedKey = CoreUtils.clone(encryptedKey);
        this.keyId = keyId;
        this.algorithm = algorithm;
    }

    /**
     * Get the encrypted key content.
     * @return The encrypted key.
     */
    public byte[] getEncryptedKey() {
        return CoreUtils.clone(encryptedKey);
    }

    /**
     * Get the key wrap algorithm used to wrap the key content.
     * @return The key wrap algorithm.
     */
    public KeyWrapAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the identifier of the key used to do encryption
     * @return the key identifier
     */
    public String getKeyId() {
        return keyId;
    }
}
