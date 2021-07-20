// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureSpi;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.NoSuchAlgorithmException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.ProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;

import static com.azure.security.keyvault.jca.KeyVaultClient.createKeyVaultClientBySystemProperty;

/**
 * key vault Rsa signature to support key less
 */
public class KeyVaultKeyLessRsaSignature extends SignatureSpi {

    // message digest implementation we use for hashing the data
    private MessageDigest messageDigest;

    // flag indicating whether the digest is reset
    private boolean digestReset = true;

    // PSS parameters from signatures and keys respectively
    // required for PSS signatures
    private PSSParameterSpec signatureParameters = null;

    private final KeyVaultClient keyVaultClient;

    private String keyId;

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

    // After throw UnsupportedOperationException, other methods will be called.
    // such as RSAPSSSignature#engineInitVerify.
    @Override
    protected void engineInitVerify(PublicKey publicKey) {
        throw new UnsupportedOperationException("getParameter() not supported");
    }

    @Override
    protected void engineInitSign(PrivateKey privateKey) {
        engineInitSign(privateKey, null);
    }

    // After throw UnsupportedOperationException, other methods will be called.
    // such as RSAPSSSignature#engineInitSign..
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
     * Ensure the object is initialized with key and parameters and
     * reset digest
     */
    private void ensureInit() throws SignatureException {
        if (this.signatureParameters == null) {
            // Parameters are required for signature verification
            throw new SignatureException("Parameters required for KeyVault signatures");
        }
    }

    /**
     * Reset the message digest if it is not already reset.
     */
    private void resetDigest() {
        if (!digestReset) {
            this.messageDigest.reset();
            digestReset = true;
        }
    }

    /**
     * Return the message digest value.
     */
    private byte[] getDigestValue() {
        digestReset = true;
        return this.messageDigest.digest();
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
        ensureInit();
        this.messageDigest.update(b);
        digestReset = false;
    }

    @Override
    protected void engineUpdate(byte[] b, int off, int len)
        throws SignatureException {
        ensureInit();
        this.messageDigest.update(b, off, len);
        digestReset = false;
    }

    @Override
    protected void engineUpdate(ByteBuffer b) {
        try {
            ensureInit();
        } catch (SignatureException se) {
            // hack for working around API bug
            throw new RuntimeException(se.getMessage());
        }
        this.messageDigest.update(b);
        digestReset = false;
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        ensureInit();
        byte[] mHash = getDigestValue();

        String encode = Base64.getEncoder().encodeToString(mHash);
        byte[] encrypted = keyVaultClient.getSignedWithPrivateKey("PS256", encode, keyId);
        return encrypted;
    }

    // After throw UnsupportedOperationException, other methods will be called.
    // such as RSAPSSSignature#engineVerify.
    @Override
    protected boolean engineVerify(byte[] sigBytes) {
        throw new UnsupportedOperationException("engineVerify() not supported");
    }

    // After throw UnsupportedOperationException, other methods will be called.
    // such as RSAPSSSignature#engineSetParameter.
    @Deprecated
    @Override
    protected void engineSetParameter(String param, Object value) {
        throw new UnsupportedOperationException("engineSetParameter(param, value) not supported");
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException {
        this.signatureParameters = (PSSParameterSpec) params;

        // disallow changing parameters when digest has been used
        if (!digestReset) {
            throw new ProviderException("Cannot set parameters during operations");
        }
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

    // After throw UnsupportedOperationException, other methods will be called.
    // such as RSAPSSSignature#engineGetParameter.
    @Deprecated
    @Override
    protected Object engineGetParameter(String param) {
        throw new UnsupportedOperationException("engineGetParameter(param) not supported");
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        AlgorithmParameters ap = null;
        if (this.signatureParameters != null) {
            try {
                ap = AlgorithmParameters.getInstance("RSASSA-PSS");
                ap.init(this.signatureParameters);
            } catch (GeneralSecurityException gse) {
                throw new ProviderException(gse.getMessage());
            }
        }
        return ap;
    }

}
