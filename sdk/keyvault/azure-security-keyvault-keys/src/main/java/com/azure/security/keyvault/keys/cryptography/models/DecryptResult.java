// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

/**
 * Represents the details of decrypt operation result.
 */
@Immutable
public final class DecryptResult {
    /**
     * The decrypted content.
     */
    private final byte[] plaintext;

    /**
     * The encrypyion algorithm used for the encryption operation.
     */
    private final EncryptionAlgorithm algorithm;

    /**
     * The identifier of the key used for the decryption operation.
     */
    private final String keyId;

    /**
     * Creates the instance of Decrypt Result holding decrypted content.
     * @param plaintext The decrypted content.
     * @param algorithm The algorithm used to decrypt the content.
     * @param keyId The identifier of the key usd for the decryption operation.
     */
    public DecryptResult(byte[] plaintext, EncryptionAlgorithm algorithm, String keyId) {
        this.plaintext = CoreUtils.clone(plaintext);
        this.algorithm = algorithm;
        this.keyId = keyId;
    }

    /**
     * Get the identifier of the key used for the decryption operation
     * @return the key identifier
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Get the encrypted content.
     * @return The decrypted content.
     */
    public byte[] getPlainText() {
        return CoreUtils.clone(plaintext);
    }

    /**
     * Get the algorithm used for decryption.
     * @return The algorithm used.
     */
    public EncryptionAlgorithm getAlgorithm() {
        return algorithm;
    }
}
