// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import java.security.AlgorithmParameters;
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
        PSSParameterSpec signatureParameters = (PSSParameterSpec) params;
        String newHashAlg = signatureParameters != null ? signatureParameters.getDigestAlgorithm() : null;
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
        return "RSASSA-PSS";
    }

    /**
     * Add this method to enable getParameters which added in this commit:
     * https://github.com/openjdk/jdk/commit/316140ff92af7ac1aadb74de9cd37a5f3c412406
     * You can find this logic in file SignatureScheme.java and line 202 in this commit.
     * Which will call this method. If we don't support this method, this algorithm won't be available
     * @return AlgorithmParameters
     */
    @Override
    protected AlgorithmParameters engineGetParameters() {
        return null;
    }

}
