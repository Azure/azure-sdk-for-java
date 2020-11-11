// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

/**
 * A class containing configuration parameters that can be applied when decrypting AES-GCM keys.
 */
public class AesGcmDecryptOptions extends DecryptOptions {
    /**
     * Creates an instance of {@link AesGcmDecryptOptions} with the given parameters.
     *
     * @param iv Initialization vector for the decryption operation.
     * @param additionalAuthenticatedData Additional data to authenticate when using authenticated crypto algorithms.
     * @param authenticationTag The tag to authenticate when performing decryption.
     */
    public AesGcmDecryptOptions(byte[] iv, byte[] additionalAuthenticatedData, byte[] authenticationTag) {
        super(iv, additionalAuthenticatedData, authenticationTag);
    }
}
