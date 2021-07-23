// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.InvalidAlgorithmParameterException;
import java.security.AlgorithmParameters;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.Base64;

import static com.azure.security.keyvault.jca.KeyVaultClient.createKeyVaultClientBySystemProperty;
import static com.azure.security.keyvault.jca.KeyVaultEncode.encodeByte;

/**
 * KeyVault EC signature to support key less
 */
public abstract class KeyVaultKeyLessECSignature extends AbstractKeyVaultKeyLessSignature {
    // message digest implementation we use
    private final MessageDigest messageDigest;

    // flag indicating whether the digest has been reset
    private boolean needsReset;

    // signature parameters
    private ECParameterSpec signatureParameters = null;

    private final String keyVaultDigestName;

    private KeyVaultClient keyVaultClient;

    private String keyId;

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
        this.needsReset = false;
    }

    //add this for test
    void setKeyVaultClient(KeyVaultClient keyVaultClient) {
        this.keyVaultClient = keyVaultClient;
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) {
        engineInitSign(privateKey, null);
    }

    // After throw UnsupportedOperationException, other methods will be called.
    // such as RSAPSSSignature#engineInitSign.
    @Override
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random) {
        if (privateKey instanceof KeyVaultPrivateKey) {
            keyId = ((KeyVaultPrivateKey) privateKey).getKid();
            resetDigest();
        } else {
            throw new UnsupportedOperationException("engineInitSign() not supported which private key is not instance of KeyVaultPrivateKey");
        }
    }

    /**
     * Resets the message digest if needed.
     */
    protected void resetDigest() {
        if (needsReset) {
            if (messageDigest != null) {
                messageDigest.reset();
            }
            needsReset = false;
        }
    }

    /**
     * Get the message digest value.
     * @return the message digest value.
     */
    protected byte[] getDigestValue() {
        needsReset = false;
        return messageDigest.digest();
    }

    @Override
    protected void engineUpdate(byte b) {
        messageDigest.update(b);
        needsReset = true;
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len) {
        messageDigest.update(b, off, len);
        needsReset = true;
    }

    @Override
    protected void engineUpdate(ByteBuffer byteBuffer) {
        int len = byteBuffer.remaining();
        if (len <= 0) {
            return;
        }

        messageDigest.update(byteBuffer);
        needsReset = true;
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

    /**
     * key vault SHA384
     */
    public static final class KeyVaultSHA384 extends KeyVaultKeyLessECSignature {
        /**
         * support SHA-384
         */
        public KeyVaultSHA384() {
            super("SHA-384", "ES384");
        }
    }

    /**
     * key vault SHA256
     */
    public static final class KeyVaultSHA256 extends KeyVaultKeyLessECSignature {
        /**
         * support SHA-256
         */
        public KeyVaultSHA256() {
            super("SHA-256", "ES256");
        }
    }

    /**
     * key vault SHA512
     */
    public static final class KeyVaultSHA512 extends KeyVaultKeyLessECSignature {
        /**
         * support SHA-512
         */
        public KeyVaultSHA512() {
            super("SHA-512", "ES512");
        }
    }

}
