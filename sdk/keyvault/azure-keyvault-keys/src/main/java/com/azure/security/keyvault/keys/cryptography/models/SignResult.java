// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.implementation.util.ImplUtils;

/**
 * Represents the details of sign operation result.
 */
public final class SignResult {

    /**
     * Creates the instance of SignResult holding the sign operation response details.
     * @param signature The signature created from the digest.
     * @param algorithm The algorithm used to sign the digest.
     */
    public SignResult(byte[] signature, SignatureAlgorithm algorithm) {
        this.signature = ImplUtils.clone(signature);
        this.algorithm = algorithm;
    }

    /**
     * The signature created from the digest.
     */
    private byte[] signature;

    /**
     * The algorithm used to create the signature.
     */
    private SignatureAlgorithm algorithm;

    /**
     * Get the signature created from the digest.
     * @return The signature.
     */
    public byte[] signature() {
        return ImplUtils.clone(signature);
    }

    /**
     * Get the signature algorithm used to create the signature.
     * @return The signature algorithm.
     */
    public SignatureAlgorithm algorithm() {
        return algorithm;
    }
}
