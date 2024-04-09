// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault Rsa signature to support key less
 */
public class KeyVaultKeylessRsa512Signature extends KeyVaultKeylessRsaSignature {

    /**
     * Construct a new KeyVaultKeyLessRsaSignature
     */
    public KeyVaultKeylessRsa512Signature() {
        super("SHA-512", "RS512");
    }

    @Override
    public String getAlgorithmName() {
        return "SHA512withRSA";
    }
}
