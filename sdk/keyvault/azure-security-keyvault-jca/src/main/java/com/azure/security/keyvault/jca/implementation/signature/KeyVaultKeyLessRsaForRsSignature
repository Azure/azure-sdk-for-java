// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import java.security.SignatureException;
import java.util.Base64;

/**
 * add RS256 Signature for service which don't support PS256
 */
public class KeyVaultKeyLessRsaForRsSignature extends AbstractKeyVaultKeyLessSignature {

    @Override
    public String getAlgorithmName() {
        return "SHA256WITHRSA";
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        byte[] mHash = getDigestValue();
        String encode = Base64.getEncoder().encodeToString(mHash);
        return keyVaultClient.getSignedWithPrivateKey("RS256", encode, keyId);
    }

}
