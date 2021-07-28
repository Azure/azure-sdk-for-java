// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.signature;

import com.azure.security.keyvault.jca.implementation.KeyVaultClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.security.AlgorithmParameters;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.Base64;

import static com.azure.security.keyvault.jca.KeyVaultEncode.encodeByte;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.createKeyVaultClientBySystemProperty;

/**
 * KeyVault EC signature to support key less
 */
public abstract class KeyVaultKeyLessECSignature extends AbstractKeyVaultKeyLessSignature {

    // signature parameters
    private ECParameterSpec signatureParameters = null;

    private final String keyVaultDigestName;

    private KeyVaultClient keyVaultClient;

    /**
     * Constructs a new KeyVaultKeyLessECSignature that will use the specified digest
     */
    KeyVaultKeyLessECSignature(String digestName, String keyVaultDigestName) {
        setKeyVaultClient(createKeyVaultClientBySystemProperty());
        try {
            messageDigest = MessageDigest.getInstance(digestName);
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        }
        this.keyVaultDigestName = keyVaultDigestName;
    }

    //add this for test
    void setKeyVaultClient(KeyVaultClient keyVaultClient) {
        this.keyVaultClient = keyVaultClient;
    }

    @Override
    protected byte[] engineSign() {
        byte[] mHash = getDigestValue();
        String encode = Base64.getEncoder().encodeToString(mHash);
        byte[] encrypted = keyVaultClient.getSignedWithPrivateKey(keyVaultDigestName, encode, keyId);
        return encodeByte(encrypted);
    }

    // After throw UnsupportedOperationException, other methods will be called.
    // such as ECDSASignature#engineSetParameter.
    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null && !(params instanceof ECParameterSpec)) {
            throw new InvalidAlgorithmParameterException("No parameter accepted");
        }
        signatureParameters = (ECParameterSpec) params;
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        if (signatureParameters == null) {
            return null;
        }
        try {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(signatureParameters);
            return parameters;
        } catch (Exception e) {
            // should never happen
            throw new ProviderException("Error retrieving EC parameters", e);
        }
    }
}
