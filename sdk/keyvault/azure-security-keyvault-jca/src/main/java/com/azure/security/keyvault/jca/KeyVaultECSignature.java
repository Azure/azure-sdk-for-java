// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.SignatureSpi;
import java.security.PrivateKey;
import java.security.InvalidParameterException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.InvalidAlgorithmParameterException;
import java.security.AlgorithmParameters;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.Base64;

import static com.azure.security.keyvault.jca.KeyVaultEncode.encodeByte;

/**
 * KeyVault EC signature to support key less
 */
public abstract class KeyVaultECSignature extends SignatureSpi {
    // message digest implementation we use
    private final MessageDigest messageDigest;

    // flag indicating whether the digest has been reset
    private boolean needsReset;

    // signature parameters
    private ECParameterSpec sigParams = null;

    // The format. true for the IEEE P1363 format. false (default) for ASN.1
    private final boolean p1363Format;

    private String digestName;

    private KeyVaultClient keyVaultClient;

    private String kid;

    /**
     * Constructs a new ECDSASignature.
     *
     * @exception ProviderException if the native ECC library is unavailable.
     */
    public KeyVaultECSignature() {
        this(false);
        initKeyVaultClient();
    }

    /**
     * init key vault client
     */
    public void initKeyVaultClient() {
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String managedIdentity = System.getProperty("azure.keyvault.managed-identity");

        keyVaultClient = new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity);
    }

    /**
     * In order to pass checkstyle
     * @param digestName digestName
     */
    public void setDigestName(String digestName) {
        this.digestName = digestName;
    }

    /**
     * Constructs a new ECDSASignature that will use the specified
     * signature format. {@code p1363Format} should be {@code true} to
     * use the IEEE P1363 format. If {@code p1363Format} is {@code false},
     * the DER-encoded ASN.1 format will be used. This constructor is
     * used by the RawECDSA subclasses.
     */
    KeyVaultECSignature(boolean p1363Format) {
        this.messageDigest = null;
        this.p1363Format = p1363Format;
    }

    /**
     * Constructs a new ECDSASignature. Used by subclasses.
     */
    KeyVaultECSignature(String digestName) {
        this(digestName, false);
    }

    /**
     * Constructs a new ECDSASignature that will use the specified
     * digest and signature format. {@code p1363Format} should be
     * {@code true} to use the IEEE P1363 format. If {@code p1363Format}
     * is {@code false}, the DER-encoded ASN.1 format will be used. This
     * constructor is used by subclasses.
     */
    KeyVaultECSignature(String digestName, boolean p1363Format) {
        initKeyVaultClient();
        try {
            messageDigest = MessageDigest.getInstance(digestName);
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        }
        switch (digestName) {
            case "SHA-256":
                this.digestName = "ES256";
                break;
            case "SHA-384":
                this.digestName = "ES384";
                break;
            default:
                this.digestName = null;
        }
        this.needsReset = false;
        this.p1363Format = p1363Format;
    }

    // initialize for verification. See JCA doc
    @Override
    protected void engineInitVerify(PublicKey publicKey) {
        throw new UnsupportedOperationException("engineInitVerify() not supported");
    }

    // initialize for signing. See JCA doc
    @Override
    protected void engineInitSign(PrivateKey privateKey) {
        engineInitSign(privateKey, null);
    }

    // initialize for signing. See JCA doc
    @Override
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random) {
        if (privateKey instanceof KeyVaultPrivateKey) {
            kid = ((KeyVaultPrivateKey) privateKey).getKid();
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

    // update the signature with the plaintext data. See JCA doc
    @Override
    protected void engineUpdate(byte b) {
        messageDigest.update(b);
        needsReset = true;
    }

    // update the signature with the plaintext data. See JCA doc
    @Override
    protected void engineUpdate(byte[] b, int off, int len) {
        messageDigest.update(b, off, len);
        needsReset = true;
    }

    // update the signature with the plaintext data. See JCA doc
    @Override
    protected void engineUpdate(ByteBuffer byteBuffer) {
        int len = byteBuffer.remaining();
        if (len <= 0) {
            return;
        }

        messageDigest.update(byteBuffer);
        needsReset = true;
    }

    // sign the data and return the signature. See JCA doc
    @Override
    protected byte[] engineSign() {

        byte[] mHash = getDigestValue();
        String encode = Base64.getEncoder().encodeToString(mHash);
        byte[] encrypted = keyVaultClient.getSignedWithPrivateKey(digestName, encode, kid);
        if (p1363Format) {
            return encrypted;
        } else {
            return encodeByte(encrypted);
        }
    }

    // verify the data and return the result. See JCA doc
    @Override
    protected boolean engineVerify(byte[] signature) {
        throw new UnsupportedOperationException("getParameter() not supported");
    }

    // set parameter, not supported. See JCA doc
    @Override
    @Deprecated
    protected void engineSetParameter(String param, Object value)
        throws InvalidParameterException {
        throw new UnsupportedOperationException("setParameter() not supported");
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null && !(params instanceof ECParameterSpec)) {
            throw new InvalidAlgorithmParameterException("No parameter accepted");
        }

        sigParams = (ECParameterSpec) params;
    }

    // get parameter, not supported. See JCA doc
    @Override
    @Deprecated
    protected Object engineGetParameter(String param)
        throws InvalidParameterException {
        throw new UnsupportedOperationException("getParameter() not supported");
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        if (sigParams == null) {
            return null;
        }
        try {
            AlgorithmParameters ap = AlgorithmParameters.getInstance("EC");
            ap.init(sigParams);
            return ap;
        } catch (Exception e) {
            // should never happen
            throw new ProviderException("Error retrieving EC parameters", e);
        }
    }

    /**
     * key vault SHA384
     */
    public static final class KeyVaultSHA384 extends KeyVaultECSignature {
        /**
         * support SHA-384
         */
        public KeyVaultSHA384() {
            super("SHA-384");
        }
    }

    /**
     * key vault SHA256
     */
    public static final class KeyVaultSHA256 extends KeyVaultECSignature {
        /**
         * support SHA-256
         */
        public KeyVaultSHA256() {
            super("SHA-256");
        }
    }

}
