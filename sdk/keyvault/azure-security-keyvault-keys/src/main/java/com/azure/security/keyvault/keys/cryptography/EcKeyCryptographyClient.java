// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import reactor.core.publisher.Mono;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Objects;

class EcKeyCryptographyClient extends LocalKeyCryptographyClient {
    private static final ClientLogger LOGGER = new ClientLogger(EcKeyCryptographyClient.class);

    private final CryptographyClientImpl serviceClient;
    private final Provider provider;

    private KeyPair keyPair;

    /**
     * Creates a EcKeyCryptographyClient that uses {@code service} to service requests
     *
     * @param serviceClient the client to use for service side cryptography operations.
     */
    EcKeyCryptographyClient(CryptographyClientImpl serviceClient) {
        super(serviceClient);

        this.serviceClient = serviceClient;
        this.provider = null;
    }

    EcKeyCryptographyClient(JsonWebKey key, CryptographyClientImpl serviceClient) {
        super(serviceClient);

        this.provider = Security.getProvider("SunEC");
        this.keyPair = key.toEc(key.hasPrivateKey(), provider);
        this.serviceClient = serviceClient;
    }

    private KeyPair getKeyPair(JsonWebKey key) {
        if (keyPair == null) {
            keyPair = key.toEc(key.hasPrivateKey());
        }

        return keyPair;
    }

    @Override
    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey key, Context context) {
        return Mono.error(new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    Mono<EncryptResult> encryptAsync(EncryptParameters options, JsonWebKey key, Context context) {
        return Mono.error(new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    EncryptResult encrypt(EncryptParameters options, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey key, Context context) {
        return Mono.error(new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    Mono<DecryptResult> decryptAsync(DecryptParameters options, JsonWebKey key, Context context) {
        return Mono.error(new UnsupportedOperationException("Decrypt operation is not supported for EC key"));
    }

    @Override
    DecryptResult decrypt(DecryptParameters options, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Decrypt operation is not supported for EC key"));
    }

    @Override
    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        keyPair = getKeyPair(key);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.signAsync(algorithm, digest, context);
            }

            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.signAsync(algorithm, digest, context);
            }

            return Mono.error(
                new IllegalArgumentException("Private portion of the key not available to perform sign operation"));
        }

        Ecdsa algo;
        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        ISignatureTransform signer = algo.createSignatureTransform(keyPair, provider);

        try {
            return Mono.just(new SignResult(signer.sign(digest), algorithm, key.getId()));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    SignResult sign(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        keyPair = getKeyPair(key);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.sign(algorithm, digest, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.sign(algorithm, digest, context);
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Private portion of the key not available to perform sign operation"));
        }

        Ecdsa algo;
        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        ISignatureTransform signer = algo.createSignatureTransform(keyPair, provider);

        try {
            return new SignResult(signer.sign(digest), algorithm, key.getId());
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) e);
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }
    }

    @Override
    Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, JsonWebKey key,
                                   Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        keyPair = getKeyPair(key);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.verifyAsync(algorithm, digest, signature, context);
            }

            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.verifyAsync(algorithm, digest, signature, context);
            }

            return Mono.error(
                new IllegalArgumentException("Public portion of the key not available to perform verify operation"));
        }

        Ecdsa algo;

        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        ISignatureTransform signer = algo.createSignatureTransform(keyPair, provider);

        try {
            return Mono.just(new VerifyResult(signer.verify(digest, signature), algorithm, key.getId()));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, JsonWebKey key,
                        Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        keyPair = getKeyPair(key);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.verify(algorithm, digest, signature, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.verify(algorithm, digest, signature, context);
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Public portion of the key not available to perform verify operation"));
        }

        Ecdsa algo;
        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        ISignatureTransform signer = algo.createSignatureTransform(keyPair, provider);

        try {
            return new VerifyResult(signer.verify(digest, signature), algorithm, key.getId());
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) e);
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }
    }

    @Override
    Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, JsonWebKey webKey, Context context) {
        return Mono.error(new UnsupportedOperationException("Wrap key operation is not supported for EC key"));
    }

    @Override
    WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, JsonWebKey webKey, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Wrap key operation is not supported for EC key"));
    }

    @Override
    Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, JsonWebKey key,
                                      Context context) {
        return Mono.error(new UnsupportedOperationException("Unwrap key operation is not supported for EC key"));
    }

    @Override
    UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Unwrap key operation is not supported for EC key"));
    }

    @Override
    Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, JsonWebKey key, Context context) {
        try {
            return signAsync(algorithm, calculateDigest(algorithm, data), key, context);
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    @Override
    SignResult signData(SignatureAlgorithm algorithm, byte[] data, JsonWebKey key, Context context) {
        try {
            return sign(algorithm, calculateDigest(algorithm, data), key, context);
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature, JsonWebKey key,
                                       Context context) {
        try {
            return verifyAsync(algorithm, calculateDigest(algorithm, data), signature, key, context);
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    @Override
    VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, JsonWebKey key,
                            Context context) {
        try {
            return verify(algorithm, calculateDigest(algorithm, data), signature, key, context);
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private byte[] calculateDigest(SignatureAlgorithm algorithm, byte[] data) throws NoSuchAlgorithmException {
        HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
        MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());

        md.update(data);

        return md.digest();
    }

    private boolean serviceClientAvailable() {
        return serviceClient != null;
    }
}
