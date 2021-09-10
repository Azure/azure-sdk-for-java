// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

import java.util.Base64;

/**
 * key vault SHA256
 */
public final class KeyVaultKeyLessRsa256Signature extends KeyVaultKeyLessECSignature {

    @Override
    public String getAlgorithmName() {
        return "SHA256withRSA";
    }

    /**
     * support SHA-256
     */
    public KeyVaultKeyLessRsa256Signature() {
        super("SHA-256", "RS256");
    }

    @Override
    protected byte[] engineSign() {
        byte[] mHash = getDigestValue();
        String encode = Base64.getEncoder().encodeToString(mHash);
        return keyVaultClient.getSignedWithPrivateKey(keyVaultDigestName, encode, keyId);
    }
}
