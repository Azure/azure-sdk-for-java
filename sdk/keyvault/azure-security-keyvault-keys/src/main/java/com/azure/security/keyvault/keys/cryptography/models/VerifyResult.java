// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

import com.azure.core.annotation.Immutable;

/**
 * Represents the details of verify operation result.
 */
@Immutable
public final class VerifyResult {
    /**
     * THe verify operation result.
     */
    private final Boolean isValid;

    /**
     * The identifier of the key used for the verify operation.
     */
    private final String keyId;

    /**
     * The algorithm used to verify the signature.
     */
    private final SignatureAlgorithm algorithm;

    /**
     * Creates the instance of Verify Result holding the verification response information.
     * @param isValid The verification info.
     * @param algorithm The algorithm used to verify the signature.
     * @param keyId The identifier of the key usd for the verify operation.
     */
    public VerifyResult(Boolean isValid, SignatureAlgorithm algorithm, String keyId) {
        this.isValid = isValid;
        this.keyId = keyId;
        this.algorithm = algorithm;
    }

    /**
     * Get the verify operation result.
     * @return The verification result.
     */
    public Boolean isValid() {
        return isValid;
    }

    /**
     * Get the signature algorithm used to verify the signature.
     * @return The signature algorithm.
     */
    public SignatureAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the identifier of the key used for the verify operation
     * @return the key identifier
     */
    public String getKeyId() {
        return keyId;
    }
}
