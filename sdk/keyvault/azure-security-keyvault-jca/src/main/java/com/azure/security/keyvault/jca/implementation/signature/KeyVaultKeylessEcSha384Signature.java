// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA384
 */
public final class KeyVaultKeylessEcSha384Signature extends KeyVaultKeylessEcSignature {
    /**
     * Algorithm name used by this implementation.
     */
    public static final String ALGORITHM_NAME = "SHA384withECDSA";

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    /**
     * support SHA-384
     */
    public KeyVaultKeylessEcSha384Signature() {
        super("SHA-384", "ES384");
    }
}
