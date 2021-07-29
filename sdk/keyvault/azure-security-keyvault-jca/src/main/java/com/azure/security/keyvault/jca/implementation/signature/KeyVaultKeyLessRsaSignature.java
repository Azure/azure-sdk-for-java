// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;

/**
 * key vault Rsa signature to support key less
 */
public class KeyVaultKeyLessRsaSignature extends AbstractKeyVaultKeyLessSignature {

    /**
     * The default algorithm for certificate sign which Key Type is RSA in key Vault will be used
     */
    public static final String RSA_ALGORITHM = "RSASSA-PSS";

    /**
     * Construct a new KeyVaultKeyLessRsaSignature
     */
    public KeyVaultKeyLessRsaSignature() {
        super();
        this.messageDigest = null;
    }

    @Override
    protected byte[] engineSign() {
        byte[] mHash = getDigestValue();
        String encode = Base64.getEncoder().encodeToString(mHash);
        //For all RSA type certificate in keyVault, we can use PS256 to encrypt.
        return keyVaultClient.getSignedWithPrivateKey("PS256", encode, keyId);
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null && !(params instanceof PSSParameterSpec)) {
            throw new InvalidAlgorithmParameterException("No parameter accepted");
        }
        String newHashAlg = ((PSSParameterSpec) params).getDigestAlgorithm();
        // re-allocate md if not yet assigned or algorithm changed
        if ((this.messageDigest == null) || !(this.messageDigest.getAlgorithm().equalsIgnoreCase(newHashAlg))) {
            try {
                this.messageDigest = MessageDigest.getInstance(newHashAlg);
            } catch (NoSuchAlgorithmException exception) {
                // should not happen as we pick default digest algorithm
                throw new InvalidAlgorithmParameterException("Unsupported digest algorithm " + newHashAlg, exception);
            }
        }
    }

    @Override
    public String getAlgorithmName() {
        //The default algorithm for certificate sign which Key Type is RSA in key Vault will be used
        return RSA_ALGORITHM;
    }
}
