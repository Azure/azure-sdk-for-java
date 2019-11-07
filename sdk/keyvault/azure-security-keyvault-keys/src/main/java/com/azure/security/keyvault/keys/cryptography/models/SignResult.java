// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

/**
 * Represents the details of sign operation result.
 */
@Immutable
public final class SignResult {
    /**
     * The signature created from the digest.
     */
    private final byte[] signature;

    /**
     * The algorithm used to create the signature.
     */
    private final SignatureAlgorithm algorithm;

    /**
     * The identifier of the key used for the verify operation.
     */
    private final String keyId;

    /**
     * Creates the instance of SignResult holding the sign operation response details.
     * @param signature The signature created from the digest.
     * @param algorithm The algorithm used to sign the digest.
     * @param keyId The identifier of the key usd for the sign operation.
     */
    public SignResult(byte[] signature, SignatureAlgorithm algorithm, String keyId) {
        this.signature = CoreUtils.clone(signature);
        this.algorithm = algorithm;
        this.keyId = keyId;
    }


    /**
     * Get the identifier of the key used for the verify operation
     * @return the key identifier
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Get the signature created from the digest.
     * @return The signature.
     */
    public byte[] getSignature() {
        return CoreUtils.clone(signature);
    }

    /**
     * Get the signature algorithm used to create the signature.
     * @return The signature algorithm.
     */
    public SignatureAlgorithm getAlgorithm() {
        return algorithm;
    }
}
