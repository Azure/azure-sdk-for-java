// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A class containing various configuration parameters that can be applied when performing encryption operations.
 */
public class EncryptOptions {
    /**
     * Initialization vector to be used in the encryption operation using a symmetric algorithm.
     */
    @JsonProperty(value = "iv")
    private final byte[] iv;

    /**
     * Get additional data to authenticate when performing encryption with an authenticated algorithm.
     */
    @JsonProperty(value = "aad")
    private final byte[] additionalAuthenticatedData;

    /**
     * Creates an instance of {@link EncryptOptions} with the given parameters.
     *
     * @param iv Initialization vector for symmetric algorithms.
     * @param additionalAuthenticatedData Additional data to authenticate but not encrypt/decrypt when using
     * authenticated crypto algorithms.
     */
    public EncryptOptions(byte[] iv, byte[] additionalAuthenticatedData) {
        if (iv == null) {
            this.iv = null;
        } else {
            this.iv = new byte[iv.length];
            System.arraycopy(iv, 0, this.iv, 0, iv.length);
        }

        if (additionalAuthenticatedData == null) {
            this.additionalAuthenticatedData = null;
        } else {
            this.additionalAuthenticatedData = new byte[additionalAuthenticatedData.length];
            System.arraycopy(additionalAuthenticatedData, 0, this.additionalAuthenticatedData, 0,
                additionalAuthenticatedData.length);
        }
    }

    /**
     * Get the initialization vector to be used in the decryption operation using a symmetric algorithm.
     *
     * @return The initialization vector.
     */
    public byte[] getIv() {
        if (iv == null) {
            return null;
        } else {
            return iv.clone();
        }
    }

    /**
     * Get additional data to authenticate when performing decryption with an authenticated algorithm.
     *
     * @return The additional authenticated data.
     */
    public byte[] getAdditionalAuthenticatedData() {
        if (additionalAuthenticatedData == null) {
            return null;
        } else {
            return additionalAuthenticatedData.clone();
        }
    }
}
