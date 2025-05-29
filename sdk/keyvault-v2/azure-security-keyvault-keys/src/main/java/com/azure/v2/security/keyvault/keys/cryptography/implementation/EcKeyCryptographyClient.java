// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyOperation;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Objects;

import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.verifyKeyPermissions;

class EcKeyCryptographyClient extends LocalKeyCryptographyClient {
    private static final ClientLogger LOGGER = new ClientLogger(EcKeyCryptographyClient.class);
    private final KeyPair ecKeyPair;
    private final Provider provider;

    EcKeyCryptographyClient(JsonWebKey jsonWebKey, CryptographyClientImpl implClient) {
        super(jsonWebKey, implClient);

        provider = Security.getProvider("SunEC");
        ecKeyPair = jsonWebKey.toEc(jsonWebKey.hasPrivateKey(), provider);
    }

    @Override
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, RequestContext requestContext) {
        throw LOGGER.throwableAtError()
            .log("The encrypt operation is not supported for EC keys.", UnsupportedOperationException::new);
    }

    @Override
    public EncryptResult encrypt(EncryptParameters options, RequestContext requestContext) {
        throw LOGGER.throwableAtError()
            .log("The encrypt operation is not supported for EC keys.", UnsupportedOperationException::new);
    }

    @Override
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] plaintext, RequestContext requestContext) {
        throw LOGGER.throwableAtError()
            .log("The decrypt operation is not supported for EC keys.", UnsupportedOperationException::new);
    }

    @Override
    public DecryptResult decrypt(DecryptParameters options, RequestContext requestContext) {
        throw LOGGER.throwableAtError()
            .log("The decrypt operation is not supported for EC keys.", UnsupportedOperationException::new);
    }

    @Override
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, RequestContext requestContext) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        // Interpret the requested algorithm.
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.sign(algorithm, digest, requestContext);
            }

            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        }

        if (ecKeyPair.getPrivate() == null) {
            if (implClient != null) {
                return implClient.sign(algorithm, digest, requestContext);
            }

            throw LOGGER.throwableAtError()
                .log("The private portion of the key is not locally available to perform the sign operation.",
                    IllegalArgumentException::new);
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.SIGN, LOGGER);

        Ecdsa algo;

        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        }

        ISignatureTransform signer = algo.createSignatureTransform(ecKeyPair, provider);

        try {
            return new SignResult(signer.sign(digest), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    @Override
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        RequestContext requestContext) {

        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        // Interpret the requested algorithm.
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.verify(algorithm, digest, signature, requestContext);
            }

            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        }

        if (ecKeyPair.getPublic() == null) {
            if (implClient != null) {
                return implClient.verify(algorithm, digest, signature, requestContext);
            }

            throw LOGGER.throwableAtError()
                .log("The public portion of the key is not locally available to perform the verify operation.",
                    IllegalArgumentException::new);
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.VERIFY, LOGGER);

        Ecdsa algo;

        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        }

        ISignatureTransform signer = algo.createSignatureTransform(ecKeyPair, provider);

        try {
            return new VerifyResult(signer.verify(digest, signature), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    @Override
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] keyToWrap, RequestContext requestContext) {
        throw LOGGER.throwableAtError()
            .log("The key wrap operation is not supported for EC keys.", UnsupportedOperationException::new);
    }

    @Override
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, RequestContext requestContext) {
        throw LOGGER.throwableAtError()
            .log("The key wrap operation is not supported for EC keys.", UnsupportedOperationException::new);
    }

    @Override
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, RequestContext requestContext) {
        return sign(algorithm, calculateDigest(algorithm, data), requestContext);
    }

    @Override
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
        RequestContext requestContext) {

        return verify(algorithm, calculateDigest(algorithm, data), signature, requestContext);
    }

    private static byte[] calculateDigest(SignatureAlgorithm algorithm, byte[] data) {
        HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance(Objects.toString(hashAlgorithm, null));
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.throwableAtError().addKeyValue("algorithm", algorithm.getValue()).log(e, CoreException::from);
        }

        md.update(data);

        return md.digest();
    }
}
