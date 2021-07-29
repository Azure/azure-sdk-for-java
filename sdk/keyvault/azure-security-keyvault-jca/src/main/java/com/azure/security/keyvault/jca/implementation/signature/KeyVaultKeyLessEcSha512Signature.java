// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA512
 */
public final class KeyVaultKeyLessEcSha512Signature extends KeyVaultKeyLessECSignature {

    /**
     * The default algorithm for certificate sign which Key Type is EC and Elliptic curve name is P-521 in key Vault will be used
     */
    public static final String EC_P_521_ALGORITHM = "SHA512withECDSA";

    @Override
    public String getAlgorithmName() {
        return EC_P_521_ALGORITHM;
    }

    /**
     * support SHA-512
     */
    public KeyVaultKeyLessEcSha512Signature() {
        super("SHA-512", "ES512");
    }

}
