// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.util.Base64;

import static com.azure.security.keyvault.jca.KeyVaultEncode.encodeByte;

/**
 * KeyVault EC signature to support key less
 */
abstract class KeyVaultKeylessEcSignature extends AbstractKeyVaultKeylessSignature {

    private final String keyVaultDigestName;

    /**
     * Constructs a new KeyVaultKeylessEcSignature that will use the specified digest
     */
    KeyVaultKeylessEcSignature(String digestName, String keyVaultDigestName) {
        super();
        try {
            messageDigest = MessageDigest.getInstance(digestName);
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        }
        this.keyVaultDigestName = keyVaultDigestName;
    }

    @Override
    protected byte[] engineSign() {
        byte[] mHash = getDigestValue();
        String encode = Base64.getEncoder().encodeToString(mHash);
        if (keyVaultClient != null) {
            byte[] encrypted = keyVaultClient.getSignedWithPrivateKey(keyVaultDigestName, encode, keyId);
            return encodeByte(encrypted);
        }
        return new byte[0];
    }

}
