// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA512
 */
public final class KeyVaultKeylessEcSha512Signature extends KeyVaultKeylessEcSignature {
    /**
     * Algorithm name used by this implementation.
     */
    public static final String ALGORITHM_NAME = "SHA512withECDSA";

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    /**
     * support SHA-512
     */
    public KeyVaultKeylessEcSha512Signature() {
        super("SHA-512", "ES512");
    }

}
