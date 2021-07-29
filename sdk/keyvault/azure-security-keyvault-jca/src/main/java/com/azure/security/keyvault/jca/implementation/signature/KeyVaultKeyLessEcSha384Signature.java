// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA384
 */
public final class KeyVaultKeyLessEcSha384Signature extends KeyVaultKeyLessECSignature {

    /**
     * The default algorithm for certificate sign which Key Type is EC and Elliptic curve name is P-384 in key Vault will be used
     */
    public static final String EC_P_384_ALGORITHM = "SHA384withECDSA";

    @Override
    public String getAlgorithmName() {
        return EC_P_384_ALGORITHM;
    }

    /**
     * support SHA-384
     */
    public KeyVaultKeyLessEcSha384Signature() {
        super("SHA-384", "ES384");
    }
}
