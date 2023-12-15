// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.implementation;

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

    private final Provider provider;

    private KeyPair keyPair;

    EcKeyCryptographyClient(JsonWebKey key, CryptographyClientImpl implClient) {
        super(implClient);

        this.provider = Security.getProvider("SunEC");
        this.keyPair = key.toEc(key.hasPrivateKey(), provider);
    }

    private KeyPair getKeyPair(JsonWebKey key) {
        if (keyPair == null) {
            keyPair = key.toEc(key.hasPrivateKey());
        }

        return keyPair;
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm,
        byte[] plaintext,
        JsonWebKey key,
        Context context) {
        return Mono.error(new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptParameters options, JsonWebKey key, Context context) {
        return Mono.error(new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    public EncryptResult encrypt(EncryptParameters options, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    public Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm,
        byte[] plaintext,
        JsonWebKey key,
        Context context) {
        return Mono.error(new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Encrypt operation is not supported for EC key"));
    }

    @Override
    public Mono<DecryptResult> decryptAsync(DecryptParameters options, JsonWebKey key, Context context) {
        return Mono.error(new UnsupportedOperationException("Decrypt operation is not supported for EC key"));
    }

    @Override
    public DecryptResult decrypt(DecryptParameters options, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Decrypt operation is not supported for EC key"));
    }

    @Override
    public Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context) {
        if (algorithm == null) {
            return Mono.error(new NullPointerException("Signature algorithm cannot be null."));
        }

        if (digest == null) {
            return Mono.error(new NullPointerException("Digest content cannot be null."));
        }

        keyPair = getKeyPair(key);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.signAsync(algorithm, digest, context);
            }

            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return implClient.signAsync(algorithm, digest, context);
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

        return Mono.fromCallable(() -> new SignResult(signer.sign(digest), algorithm, key.getId()));
    }

    @Override
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        keyPair = getKeyPair(key);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.sign(algorithm, digest, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return implClient.sign(algorithm, digest, context);
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
    public Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm,
        byte[] digest,
        byte[] signature,
        JsonWebKey key,
        Context context) {
        if (algorithm == null) {
            return Mono.error(new NullPointerException("Signature algorithm cannot be null."));
        }

        if (digest == null) {
            return Mono.error(new NullPointerException("Digest content cannot be null."));
        }

        if (signature == null) {
            return Mono.error(new NullPointerException("Signature to be verified cannot be null."));
        }

        keyPair = getKeyPair(key);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.verifyAsync(algorithm, digest, signature, context);
            }

            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return implClient.verifyAsync(algorithm, digest, signature, context);
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

        return Mono.fromCallable(() -> new VerifyResult(signer.verify(digest, signature), algorithm, key.getId()));
    }

    @Override
    public VerifyResult verify(SignatureAlgorithm algorithm,
        byte[] digest,
        byte[] signature,
        JsonWebKey key,
        Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        keyPair = getKeyPair(key);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.verify(algorithm, digest, signature, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return implClient.verify(algorithm, digest, signature, context);
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
    public Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, JsonWebKey webKey, Context context) {
        return Mono.error(new UnsupportedOperationException("Wrap key operation is not supported for EC key"));
    }

    @Override
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, JsonWebKey webKey, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Wrap key operation is not supported for EC key"));
    }

    @Override
    public Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm,
        byte[] encryptedKey,
        JsonWebKey key,
        Context context) {
        return Mono.error(new UnsupportedOperationException("Unwrap key operation is not supported for EC key"));
    }

    @Override
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Unwrap key operation is not supported for EC key"));
    }

    @Override
    public Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, JsonWebKey key, Context context) {
        try {
            return signAsync(algorithm, calculateDigest(algorithm, data), key, context);
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    @Override
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, JsonWebKey key, Context context) {
        try {
            return sign(algorithm, calculateDigest(algorithm, data), key, context);
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    public Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm,
        byte[] data,
        byte[] signature,
        JsonWebKey key,
        Context context) {
        try {
            return verifyAsync(algorithm, calculateDigest(algorithm, data), signature, key, context);
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    @Override
    public VerifyResult verifyData(SignatureAlgorithm algorithm,
        byte[] data,
        byte[] signature,
        JsonWebKey key,
        Context context) {
        try {
            return verify(algorithm, calculateDigest(algorithm, data), signature, key, context);
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private static byte[] calculateDigest(SignatureAlgorithm algorithm, byte[] data) throws NoSuchAlgorithmException {
        HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
        MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());

        md.update(data);

        return md.digest();
    }

    private boolean serviceClientAvailable() {
        return implClient != null;
    }
}
