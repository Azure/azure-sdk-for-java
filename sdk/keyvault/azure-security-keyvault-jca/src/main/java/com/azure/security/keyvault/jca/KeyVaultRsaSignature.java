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

/**
 * key vault Rsa signature to support key less
 */
public class KeyVaultRsaSignature extends SignatureSpi {

    // message digest implementation we use for hashing the data
    private MessageDigest md;
    // flag indicating whether the digest is reset
    private boolean digestReset = true;

    // PSS parameters from signatures and keys respectively
    private PSSParameterSpec sigParams = null; // required for PSS signatures

    private final KeyVaultClient keyVaultClient;

    private String alias;

    private String version;

    /**
     * Construct a new RSAPSSSignatur with arbitrary digest algorithm
     */
    public KeyVaultRsaSignature() {
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String managedIdentity = System.getProperty("azure.keyvault.managed-identity");

        keyVaultClient = new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity);
        this.md = null;
    }

    // initialize for verification. See JCA doc
    @Override
    protected void engineInitVerify(PublicKey publicKey) {
        throw new UnsupportedOperationException("getParameter() not supported");
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
            alias = privateKey.getFormat();
            version = new String(privateKey.getEncoded());
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
        if (this.sigParams == null) {
            // Parameters are required for signature verification
            throw new SignatureException("Parameters required for KeyVault signatures");
        }
    }

    /**
     * Reset the message digest if it is not already reset.
     */
    private void resetDigest() {
        if (!digestReset) {
            this.md.reset();
            digestReset = true;
        }
    }

    /**
     * Return the message digest value.
     */
    private byte[] getDigestValue() {
        digestReset = true;
        return this.md.digest();
    }

    // update the signature with the plaintext data. See JCA doc
    @Override
    protected void engineUpdate(byte b) throws SignatureException {
        ensureInit();
        this.md.update(b);
        digestReset = false;
    }

    // update the signature with the plaintext data. See JCA doc
    @Override
    protected void engineUpdate(byte[] b, int off, int len)
        throws SignatureException {
        ensureInit();
        this.md.update(b, off, len);
        digestReset = false;
    }

    // update the signature with the plaintext data. See JCA doc
    @Override
    protected void engineUpdate(ByteBuffer b) {
        try {
            ensureInit();
        } catch (SignatureException se) {
            // hack for working around API bug
            throw new RuntimeException(se.getMessage());
        }
        this.md.update(b);
        digestReset = false;
    }

    // sign the data and return the signature. See JCA doc
    @Override
    protected byte[] engineSign() throws SignatureException {
        ensureInit();
        byte[] mHash = getDigestValue();

        String encode = Base64.getEncoder().encodeToString(mHash);
        byte[] encrypted = keyVaultClient.getSignedWithPrivateKey("PS256", encode, alias, version);
        return encrypted;
    }

    // verify the data and return the result. See JCA doc
    // should be reset to the state after engineInitVerify call.
    @Override
    protected boolean engineVerify(byte[] sigBytes) {
        throw new UnsupportedOperationException("engineVerify() not supported");
    }

    // set parameter, not supported. See JCA doc
    @Deprecated
    @Override
    protected void engineSetParameter(String param, Object value) {
        throw new UnsupportedOperationException("engineSetParameter(param, value) not supported");
    }

    @Override
    protected void engineSetParameter(AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException {
        this.sigParams = (PSSParameterSpec) params;

        // disallow changing parameters when digest has been used
        if (!digestReset) {
            throw new ProviderException("Cannot set parameters during operations");
        }
        String newHashAlg = this.sigParams.getDigestAlgorithm();
        // re-allocate md if not yet assigned or algorithm changed
        if ((this.md == null) || !(this.md.getAlgorithm().equalsIgnoreCase(newHashAlg))) {
            try {
                this.md = MessageDigest.getInstance(newHashAlg);
            } catch (NoSuchAlgorithmException nsae) {
                // should not happen as we pick default digest algorithm
                throw new InvalidAlgorithmParameterException("Unsupported digest algorithm " + newHashAlg, nsae);
            }
        }
    }

    // get parameter, not supported. See JCA doc
    @Deprecated
    @Override
    protected Object engineGetParameter(String param) {
        throw new UnsupportedOperationException("engineGetParameter(param) not supported");
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        AlgorithmParameters ap = null;
        if (this.sigParams != null) {
            try {
                ap = AlgorithmParameters.getInstance("RSASSA-PSS");
                ap.init(this.sigParams);
            } catch (GeneralSecurityException gse) {
                throw new ProviderException(gse.getMessage());
            }
        }
        return ap;
    }

}
