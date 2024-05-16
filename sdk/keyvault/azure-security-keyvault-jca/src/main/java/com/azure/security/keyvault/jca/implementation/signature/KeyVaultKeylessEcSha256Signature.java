// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA256
 */
public final class KeyVaultKeylessEcSha256Signature extends KeyVaultKeylessEcSignature {

    @Override
    public String getAlgorithmName() {
        return "SHA256withECDSA";
    }

    /**
     * support SHA-256
     */
    public KeyVaultKeylessEcSha256Signature() {
        super("SHA-256", "ES256");
    }


}
