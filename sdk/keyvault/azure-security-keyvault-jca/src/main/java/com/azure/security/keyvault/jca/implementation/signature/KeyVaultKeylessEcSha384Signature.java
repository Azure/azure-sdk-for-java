// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA384
 */
public final class KeyVaultKeylessEcSha384Signature extends KeyVaultKeylessEcSignature {

    @Override
    public String getAlgorithmName() {
        return "SHA384withECDSA";
    }

    /**
     * support SHA-384
     */
    public KeyVaultKeylessEcSha384Signature() {
        super("SHA-384", "ES384");
    }
}
