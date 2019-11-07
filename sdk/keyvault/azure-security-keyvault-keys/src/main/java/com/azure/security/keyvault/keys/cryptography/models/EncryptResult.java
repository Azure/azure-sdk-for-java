// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

/**
 * Represents the details of encrypt operation result.
 */
@Immutable
public final class EncryptResult {
    /**
     * The encrypted content.
     */
    private final byte[] cipherText;

    /**
     * The algorithm used for the encryption operation.
     */
    private final EncryptionAlgorithm algorithm;

    /**
     * The identifier of the key used for the encryption operation.
     */
    private final String keyId;

    /**
     * Creates the instance of Encrypt Result holding encryption operation response information.
     * @param cipherText The encrypted content.
     * @param algorithm The algorithm used to encrypt the content.
     * @param keyId The identifier of the key usd for the encryption operation.
     */
    public EncryptResult(byte[] cipherText, EncryptionAlgorithm algorithm, String keyId) {
        this.cipherText = CoreUtils.clone(cipherText);
        this.algorithm = algorithm;
        this.keyId = keyId;
    }

    /**
     * Get the identifier of the key used to do encryption
     * @return the key identifier
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Get the encrypted content.
     * @return The encrypted content.
     */
    public byte[] getCipherText() {
        return CoreUtils.clone(cipherText);
    }

    /**
     * Get the encryption algorithm used for encryption.
     * @return The encryption algorithm used.
     */
    public EncryptionAlgorithm getAlgorithm() {
        return algorithm;
    }
}
