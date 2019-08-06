// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.implementation.util.ImplUtils;

/**
 * Represents the details of encrypt operation result.
 */
public final class EncryptResult {

    /**
     * Creates the instance of Encrypt Result holding encryption operation response information.
     * @param cipherText The encrypted content.
     * @param authenticationTag The authentication tag.
     * @param algorithm The algorithm used to encrypt the content.
     */
    public EncryptResult(byte[] cipherText, byte[] authenticationTag, EncryptionAlgorithm algorithm) {
        this.cipherText = ImplUtils.clone(cipherText);
        this.authenticationTag = ImplUtils.clone(authenticationTag);
        this.algorithm = algorithm;
    }

    /**
     * THe encrypted content.
     */
    private byte[] cipherText;

    /**
     * The authentication tag.
     */
    private byte[] authenticationTag;

    /**
     * The encrypyion algorithm used for the encryption operation.
     */
    private EncryptionAlgorithm algorithm;

    /**
     * Get the encrypted content.
     * @return The encrypted content.
     */
    public byte[] cipherText() {
        return ImplUtils.clone(cipherText);
    }

    /**
     * Get the authentication tag.
     * @return The authentication tag.
     */
    public byte[] authenticationTag() {
        return ImplUtils.clone(authenticationTag);
    }

    /**
     * Get the encryption algorithm used for encryption.
     * @return The encryption algorithm used.
     */
    public EncryptionAlgorithm algorithm() {
        return algorithm;
    }

}
