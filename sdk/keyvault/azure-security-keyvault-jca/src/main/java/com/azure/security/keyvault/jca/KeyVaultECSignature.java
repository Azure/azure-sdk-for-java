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

    private String digestName;

    private KeyVaultClient keyVaultClient;

    private String kid;

    /**
     * Constructs a new KeyVaultECSignature.
     *
     * @exception ProviderException if the native ECC library is unavailable.
     */
    public KeyVaultECSignature() {
        this.messageDigest = null;
        initKeyVaultClient();
    }

    /**
     * init key vault client
     */
    //TODO (zhicliu): Should we provide KeyVaultClient through privateKey to ensure that there is only one best practice?
    private void initKeyVaultClient() {
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String managedIdentity = System.getProperty("azure.keyvault.managed-identity");

        keyVaultClient = new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity);
    }

    void setKeyVaultClient(KeyVaultClient keyVaultClient) {
        this.keyVaultClient = keyVaultClient;
    }

    /**
     * In order to pass checkstyle
     * @param digestName digestName
     */
    public void setDigestName(String digestName) {
        this.digestName = digestName;
    }

    /**
     * Constructs a new KeyVaultECSignature that will use the specified digest
     */
    KeyVaultECSignature(String digestName) {
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
    }

    //This class is only used for keyLess signatures, other functions will
    //call other suitable algorithms, such as ECDSASignature
    @Override
    protected void engineInitVerify(PublicKey publicKey) {
        throw new UnsupportedOperationException("engineInitVerify() not supported");
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) {
        engineInitSign(privateKey, null);
    }

    //This class is only used for keyLess signatures, other functions will
    //call other suitable algorithms, such as ECDSASignature
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
        byte[] encrypted = keyVaultClient.getSignedWithPrivateKey(digestName, encode, kid);
        return encodeByte(encrypted);
    }

    //This class is only used for keyLess signatures, other functions will
    //call other suitable algorithms, such as ECDSASignature
    @Override
    protected boolean engineVerify(byte[] signature) {
        throw new UnsupportedOperationException("getParameter() not supported");
    }

    //This class is only used for keyLess signatures, other functions will
    //call other suitable algorithms, such as ECDSASignature
    @Override
    @Deprecated
    protected void engineSetParameter(String param, Object value)
        throws InvalidParameterException {
        throw new UnsupportedOperationException("setParameter() not supported");
    }

    //This class is only used for keyLess signatures, other functions will
    //call other suitable algorithms, such as ECDSASignature
    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException {
        if (params != null && !(params instanceof ECParameterSpec)) {
            throw new InvalidAlgorithmParameterException("No parameter accepted");
        }

        sigParams = (ECParameterSpec) params;
    }

    //This class is only used for keyLess signatures, other functions will
    //call other suitable algorithms, such as ECDSASignature
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
