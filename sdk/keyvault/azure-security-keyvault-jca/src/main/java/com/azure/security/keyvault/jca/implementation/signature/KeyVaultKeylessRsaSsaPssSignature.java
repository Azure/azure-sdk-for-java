// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.PSSParameterSpec;

/**
 * key vault Rsa signature to support key less
 */
public class KeyVaultKeylessRsaSsaPssSignature extends KeyVaultKeylessRsaSignature {

    /**
     * Construct a new KeyVaultKeyLessRsaSignature
     */
    public KeyVaultKeylessRsaSsaPssSignature() {
        //For all RSA type certificate in keyVault, we can use PS256 to encrypt.
        super(null, "PS256");
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException {
        if (params == null) {
            throw new InvalidAlgorithmParameterException("Parameters cannot be null");
        }
        if (!(params instanceof PSSParameterSpec)) {
            throw new InvalidAlgorithmParameterException("No parameter accepted");
        }
        PSSParameterSpec signatureParameters = (PSSParameterSpec) params;
        String newHashAlg = signatureParameters.getDigestAlgorithm();
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
}
