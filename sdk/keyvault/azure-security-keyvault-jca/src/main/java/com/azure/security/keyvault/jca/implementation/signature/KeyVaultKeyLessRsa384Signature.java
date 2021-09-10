// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.signature;

import java.util.Base64;

/**
 * key vault SHA384
 */
public final class KeyVaultKeyLessRsa384Signature extends KeyVaultKeyLessECSignature {

    @Override
    public String getAlgorithmName() {
        return "SHA384withRSA";
    }

    /**
     * support SHA-384
     */
    public KeyVaultKeyLessRsa384Signature() {
        super("SHA-384", "RS384");
    }

    @Override
    protected byte[] engineSign() {
        byte[] mHash = getDigestValue();
        String encode = Base64.getEncoder().encodeToString(mHash);
        return keyVaultClient.getSignedWithPrivateKey(keyVaultDigestName, encode, keyId);
    }
}
