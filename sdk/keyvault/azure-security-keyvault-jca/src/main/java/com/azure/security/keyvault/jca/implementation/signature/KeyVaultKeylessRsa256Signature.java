// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault Rsa signature to support key less
 */
public class KeyVaultKeylessRsa256Signature extends KeyVaultKeylessRsaSignature {
    /**
     * Algorithm name used by this implementation.
     */
    public static final String ALGORITHM_NAME = "SHA256withRSA";

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    /**
     * Construct a new KeyVaultKeyLessRsaSignature
     */
    public KeyVaultKeylessRsa256Signature() {
        super("SHA-256", "RS256");
    }
}
