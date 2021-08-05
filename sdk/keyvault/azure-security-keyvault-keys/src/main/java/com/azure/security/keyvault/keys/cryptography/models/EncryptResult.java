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
    private final byte[] ciphertext;

    /**
     * The algorithm used for the encryption operation.
     */
    private final EncryptionAlgorithm algorithm;

    /**
     * The identifier of the key used for the encryption operation.
     */
    private final String keyId;

    /**
     * Initialization vector for symmetric algorithms.
     */
    private final byte[] iv;

    /**
     * The tag to authenticate when performing decryption with an authenticated algorithm.
     */
    private final byte[] authenticationTag;

    /**
     * Additional data to authenticate but not encrypt/decrypt when using authenticated crypto algorithms.
     */
    private final byte[] additionalAuthenticatedData;


    /**
     * Creates the instance of Encrypt Result holding encryption operation response information.
     * @param ciphertext The encrypted content.
     * @param algorithm The algorithm used to encrypt the content.
     * @param keyId The identifier of the key usd for the encryption operation.
     */
    public EncryptResult(byte[] ciphertext, EncryptionAlgorithm algorithm, String keyId) {
        this(ciphertext, algorithm, keyId, null, null, null);
    }

    /**
     * Creates the instance of Encrypt Result holding encryption operation response information.
     * @param ciphertext The encrypted content.
     * @param algorithm The algorithm used to encrypt the content.
     * @param keyId The identifier of the key usd for the encryption operation.
     * @param iv Initialization vector for symmetric algorithms.
     * @param authenticationTag The tag to authenticate when performing decryption with an authenticated algorithm.
     * @param additionalAuthenticatedData Additional data to authenticate but not encrypt/decrypt when using authenticated crypto algorithms.
     */
    public EncryptResult(byte[] ciphertext, EncryptionAlgorithm algorithm, String keyId, byte[] iv,
                         byte[] authenticationTag, byte[] additionalAuthenticatedData) {
        this.ciphertext = CoreUtils.clone(ciphertext);
        this.algorithm = algorithm;
        this.keyId = keyId;
        this.iv = CoreUtils.clone(iv);
        this.authenticationTag = CoreUtils.clone(authenticationTag);
        this.additionalAuthenticatedData = CoreUtils.clone(additionalAuthenticatedData);
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
        return CoreUtils.clone(ciphertext);
    }

    /**
     * Get the encryption algorithm used for encryption.
     * @return The encryption algorithm used.
     */
    public EncryptionAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the initialization vector used by symmetric algorithms.
     *
     * @return The initialization vector.
     */
    public byte[] getIv() {
        return CoreUtils.clone(iv);
    }

    /**
     * Get the tag to authenticate the encrypted content.
     *
     * @return The authentication tag.
     */
    public byte[] getAuthenticationTag() {
        return CoreUtils.clone(authenticationTag);
    }

    /**
     * Get additional data to authenticate the encrypted content.
     *
     * @return The additional authenticated data.
     */
    public byte[] getAdditionalAuthenticatedData() {
        return CoreUtils.clone(additionalAuthenticatedData);
    }
}
