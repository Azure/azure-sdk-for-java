// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault Rsa signature to support key less
 */
public class KeyVaultKeylessRsa256Signature extends KeyVaultKeylessRsaSignature {

    /**
     * Construct a new KeyVaultKeyLessRsaSignature
     */
    public KeyVaultKeylessRsa256Signature() {
        super("SHA-256", "RS256");
    }

    @Override
    public String getAlgorithmName() {
        return "SHA256withRSA";
    }
}
