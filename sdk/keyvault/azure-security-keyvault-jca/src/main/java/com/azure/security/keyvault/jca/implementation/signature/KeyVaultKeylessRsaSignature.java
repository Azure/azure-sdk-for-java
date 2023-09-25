// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.util.Base64;

/**
 * key vault Rsa signature to support key less
 */
abstract class KeyVaultKeylessRsaSignature extends AbstractKeyVaultKeylessSignature {

    private final String keyVaultDigestName;

    /**
     * Construct a new KeyVaultKeyLessRsaSignature
     */
    KeyVaultKeylessRsaSignature(String digestName, String keyVaultDigestName) {
        if (digestName != null) {
            try {
                messageDigest = MessageDigest.getInstance(digestName);
            } catch (NoSuchAlgorithmException e) {
                throw new ProviderException(e);
            }
        }
        this.keyVaultDigestName = keyVaultDigestName;
    }

    @Override
    protected byte[] engineSign() {
        byte[] mHash = getDigestValue();
        String encode = Base64.getEncoder().encodeToString(mHash);
        if (keyVaultClient != null) {
            return keyVaultClient.getSignedWithPrivateKey(this.keyVaultDigestName, encode, keyId);
        }
        return new byte[0];
    }

}
