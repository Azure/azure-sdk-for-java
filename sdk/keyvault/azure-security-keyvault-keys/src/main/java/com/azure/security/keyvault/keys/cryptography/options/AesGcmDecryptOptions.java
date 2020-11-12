// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.options;

import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

/**
 * A class containing configuration parameters that can be applied when decrypting AES-GCM keys.
 */
public class AesGcmDecryptOptions extends DecryptOptions {
    /**
     * Creates an instance of {@link AesGcmDecryptOptions} with the given parameters.
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     * @param iv Initialization vector for the decryption operation.
     */
    AesGcmDecryptOptions(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv) {
        super(algorithm, ciphertext);

        if (iv == null) {
            this.iv = null;
        } else {
            this.iv = new byte[iv.length];
            System.arraycopy(iv, 0, this.iv, 0, iv.length);
        }
    }

    /**
     * Set additional data to authenticate when using authenticated crypto algorithms.
     *
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @return The updated {@link AesGcmDecryptOptions} object.
     */
    public AesGcmDecryptOptions setAdditionalAuthenticatedData(byte[] additionalAuthenticatedData) {
        if (additionalAuthenticatedData == null) {
            this.additionalAuthenticatedData = null;
        } else {
            this.additionalAuthenticatedData = new byte[additionalAuthenticatedData.length];
            System.arraycopy(additionalAuthenticatedData, 0, this.additionalAuthenticatedData, 0,
                additionalAuthenticatedData.length);
        }

        return this;
    }

    /**
     * Set the tag to authenticate when performing decryption.
     *
     * @param authenticationTag The tag to authenticate when performing decryption.
     * @return The updated {@link AesGcmDecryptOptions} object.
     */
    public AesGcmDecryptOptions setAuthenticationTag(byte[] authenticationTag) {
        if (authenticationTag == null) {
            this.authenticationTag = null;
        } else {
            this.authenticationTag = new byte[authenticationTag.length];
            System.arraycopy(authenticationTag, 0, this.authenticationTag, 0,
                authenticationTag.length);
        }

        return this;
    }
}
