// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import com.azure.security.keyvault.jca.implementation.KeyVaultClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.ProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;

import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.createKeyVaultClientBySystemProperty;

/**
 * key vault Rsa signature to support key less
 */
public class KeyVaultKeyLessRsaSignature extends AbstractKeyVaultKeyLessSignature {

    // PSS parameters from signatures and keys respectively
    // required for PSS signatures
    private PSSParameterSpec signatureParameters = null;

    private final KeyVaultClient keyVaultClient;

    /**
     * Construct a new KeyVaultKeyLessRsaSignature
     */
    public KeyVaultKeyLessRsaSignature() {
        this(createKeyVaultClientBySystemProperty());
    }

    /**
     * Construct a new KeyVaultKeyLessRsaSignature with key vault client
     * @param keyVaultClient keyVaultClient
     */
    public KeyVaultKeyLessRsaSignature(KeyVaultClient keyVaultClient) {
        this.keyVaultClient = keyVaultClient;
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
        this.signatureParameters = (PSSParameterSpec) params;
        String newHashAlg = this.signatureParameters.getDigestAlgorithm();
        // re-allocate md if not yet assigned or algorithm changed
        if ((this.messageDigest == null) || !(this.messageDigest.getAlgorithm().equalsIgnoreCase(newHashAlg))) {
            try {
                this.messageDigest = MessageDigest.getInstance(newHashAlg);
            } catch (NoSuchAlgorithmException nsae) {
                // should not happen as we pick default digest algorithm
                throw new InvalidAlgorithmParameterException("Unsupported digest algorithm " + newHashAlg, nsae);
            }
        }
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        AlgorithmParameters parameters = null;
        if (this.signatureParameters != null) {
            try {
                parameters = AlgorithmParameters.getInstance("RSASSA-PSS");
                parameters.init(this.signatureParameters);
            } catch (GeneralSecurityException gse) {
                throw new ProviderException(gse.getMessage());
            }
        }
        return parameters;
    }

}
