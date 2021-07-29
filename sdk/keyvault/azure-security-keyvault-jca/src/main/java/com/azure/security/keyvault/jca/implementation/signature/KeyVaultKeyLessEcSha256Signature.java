// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

/**
 * key vault SHA256
 */
public final class KeyVaultKeyLessEcSha256Signature extends KeyVaultKeyLessECSignature {

    /**
     * The default algorithm for certificate sign which Key Type is EC and Elliptic curve name is P-256 in key Vault will be used
     */
    public static final String EC_P_256_ALGORITHM = "SHA256withECDSA";

    @Override
    public String getAlgorithmName() {
        return EC_P_256_ALGORITHM;
    }

    /**
     * support SHA-256
     */
    public KeyVaultKeyLessEcSha256Signature() {
        super("SHA-256", "ES256");
    }


}
