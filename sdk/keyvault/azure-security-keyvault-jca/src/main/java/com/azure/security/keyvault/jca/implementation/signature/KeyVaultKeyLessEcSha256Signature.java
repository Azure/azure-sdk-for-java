// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA256
 */
public final class KeyVaultKeyLessEcSha256Signature extends KeyVaultKeyLessECSignature {

    @Override
    public String getAlgorithmName() {
        return "SHA256withECDSA";
    }

    /**
     * support SHA-256
     */
    public KeyVaultKeyLessEcSha256Signature() {
        super("SHA-256", "ES256");
    }


}
