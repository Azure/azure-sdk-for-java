// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

import java.util.Base64;

/**
 * key vault SHA512
 */
public final class KeyVaultKeyLessRsa512Signature extends KeyVaultKeyLessECSignature {

    @Override
    public String getAlgorithmName() {
        return "SHA512withRSA";
    }

    /**
     * support SHA-512
     */
    public KeyVaultKeyLessRsa512Signature() {
        super("SHA-512", "RS512");
    }

    @Override
    protected byte[] engineSign() {
        byte[] mHash = getDigestValue();
        String encode = Base64.getEncoder().encodeToString(mHash);
        return keyVaultClient.getSignedWithPrivateKey(keyVaultDigestName, encode, keyId);
    }
}
