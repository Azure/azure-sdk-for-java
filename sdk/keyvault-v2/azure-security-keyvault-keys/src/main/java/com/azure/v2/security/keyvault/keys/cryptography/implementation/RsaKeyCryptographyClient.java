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
import java.util.Objects;

import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.verifyKeyPermissions;

class RsaKeyCryptographyClient extends LocalKeyCryptographyClient {
    private static final ClientLogger LOGGER = new ClientLogger(RsaKeyCryptographyClient.class);
    private final KeyPair rsaKeyPair;

    RsaKeyCryptographyClient(JsonWebKey jsonWebKey, CryptographyClientImpl implClient) {
        super(jsonWebKey, implClient);

        rsaKeyPair = jsonWebKey.toRsa(jsonWebKey.hasPrivateKey());
    }

    @Override
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, RequestContext requestContext) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.encrypt(algorithm, plaintext, null, null, requestContext);
            }

            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        }

        if (rsaKeyPair.getPublic() == null) {
            if (implClient != null) {
                return implClient.encrypt(algorithm, plaintext, null, null, requestContext);
            }

            throw LOGGER.throwableAtError()
                .log("The public portion of the key is not available to perform the encrypt operation.",
                    IllegalArgumentException::new);
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.ENCRYPT, LOGGER);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createEncryptor(rsaKeyPair);

            return new EncryptResult(transform.doFinal(plaintext), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    @Override
    public EncryptResult encrypt(EncryptParameters encryptParameters, RequestContext requestContext) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        return encrypt(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(), requestContext);
    }

    @Override
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, RequestContext requestContext) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.decrypt(algorithm, ciphertext, requestContext);
            }

            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        }

        if (rsaKeyPair.getPrivate() == null) {
            if (implClient != null) {
                return implClient.decrypt(algorithm, ciphertext, requestContext);
            }

            throw LOGGER.throwableAtError()
                .log("The private portion of the key is not available to perform the decrypt operation.",
                    IllegalArgumentException::new);
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.DECRYPT, LOGGER);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createDecryptor(rsaKeyPair);

            return new DecryptResult(transform.doFinal(ciphertext), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    @Override
    public DecryptResult decrypt(DecryptParameters decryptParameters, RequestContext requestContext) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        return decrypt(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(), requestContext);
    }

    @Override
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, RequestContext requestContext) {
        if (implClient != null) {
            return implClient.sign(algorithm, digest, requestContext);
        } else {
            throw LOGGER.throwableAtError()
                .log("The sign operation on a local RSA key is not currently supported.",
                    UnsupportedOperationException::new);
        }
    }

    @Override
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        RequestContext requestContext) {
        if (implClient != null) {
            return implClient.verify(algorithm, digest, signature, requestContext);
        } else {
            throw LOGGER.throwableAtError()
                .log("The verify operation on a local RSA key is not currently supported.",
                    UnsupportedOperationException::new);
        }
    }

    @Override
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] keyToWrap, RequestContext requestContext) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(keyToWrap, "Key content to be wrapped cannot be null.");

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.wrapKey(algorithm, keyToWrap, requestContext);
            }

            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        }

        if (rsaKeyPair.getPublic() == null) {
            if (implClient != null) {
                return implClient.wrapKey(algorithm, keyToWrap, requestContext);
            }

            throw LOGGER.throwableAtError()
                .log("The public portion of the key is not available to perform the key wrap operation.",
                    IllegalArgumentException::new);
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.WRAP_KEY, LOGGER);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createEncryptor(rsaKeyPair);

            return new WrapResult(transform.doFinal(keyToWrap), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    @Override
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, RequestContext requestContext) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.unwrapKey(algorithm, encryptedKey, requestContext);
            }

            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        }

        if (rsaKeyPair.getPrivate() == null) {
            if (implClient != null) {
                return implClient.unwrapKey(algorithm, encryptedKey, requestContext);
            }

            throw LOGGER.throwableAtError()
                .log("The public portion of the key is not available to perform the key wrap operation.",
                    IllegalArgumentException::new);
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.UNWRAP_KEY, LOGGER);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createDecryptor(rsaKeyPair);

            return new UnwrapResult(transform.doFinal(encryptedKey), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
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
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log(e, IllegalArgumentException::new);
        }

        md.update(data);

        return md.digest();
    }
}
