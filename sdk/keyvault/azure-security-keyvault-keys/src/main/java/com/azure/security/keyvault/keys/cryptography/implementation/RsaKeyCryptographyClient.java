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

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

class RsaKeyCryptographyClient extends LocalKeyCryptographyClient {
    private static final ClientLogger LOGGER = new ClientLogger(RsaKeyCryptographyClient.class);

    private KeyPair keyPair;

    RsaKeyCryptographyClient(JsonWebKey key, CryptographyClientImpl implClient) {
        super(implClient);

        keyPair = key.toRsa(key.hasPrivateKey());
    }

    private KeyPair getKeyPair(JsonWebKey key) {
        if (keyPair == null) {
            keyPair = key.toRsa(key.hasPrivateKey());
        }

        return keyPair;
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm,
        byte[] plaintext,
        JsonWebKey jsonWebKey,
        Context context) {
        if (algorithm == null) {
            return Mono.error(new NullPointerException("Encryption algorithm cannot be null."));
        }

        if (plaintext == null) {
            return Mono.error(new NullPointerException("Plaintext cannot be null."));
        }

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.encryptAsync(algorithm, plaintext, context);
            }

            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return implClient.encryptAsync(algorithm, plaintext, context);
            }

            return Mono.error(new IllegalArgumentException(
                "Public portion of the key not available to perform encrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            ICryptoTransform transform = algo.createEncryptor(keyPair);

            return new EncryptResult(transform.doFinal(plaintext), algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public EncryptResult encrypt(EncryptionAlgorithm algorithm,
        byte[] plaintext,
        JsonWebKey jsonWebKey,
        Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.encrypt(algorithm, plaintext, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return implClient.encrypt(algorithm, plaintext, context);
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Public portion of the key not available to perform encrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createEncryptor(keyPair);

            return new EncryptResult(transform.doFinal(plaintext), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptParameters encryptParameters, JsonWebKey jsonWebKey, Context context) {
        return Mono.fromCallable(() -> encrypt(encryptParameters, jsonWebKey, context));
    }

    @Override
    public EncryptResult encrypt(EncryptParameters encryptParameters, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        return encrypt(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(), jsonWebKey, context);
    }

    @Override
    public Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm,
        byte[] ciphertext,
        JsonWebKey jsonWebKey,
        Context context) {
        if (algorithm == null) {
            return Mono.error(new NullPointerException("Encryption algorithm cannot be null."));
        }

        if (ciphertext == null) {
            return Mono.error(new NullPointerException("Ciphertext cannot be null."));
        }

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.decryptAsync(algorithm, ciphertext, context);
            }

            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return implClient.decryptAsync(algorithm, ciphertext, context);
            }

            return Mono.error(new IllegalArgumentException(
                "Private portion of the key not available to perform decrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            ICryptoTransform transform = algo.createDecryptor(keyPair);

            return new DecryptResult(transform.doFinal(ciphertext), algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public DecryptResult decrypt(EncryptionAlgorithm algorithm,
        byte[] ciphertext,
        JsonWebKey jsonWebKey,
        Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.decrypt(algorithm, ciphertext, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return implClient.decrypt(algorithm, ciphertext, context);
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Private portion of the key not available to perform decrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createDecryptor(keyPair);

            return new DecryptResult(transform.doFinal(ciphertext), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    public Mono<DecryptResult> decryptAsync(DecryptParameters decryptParameters, JsonWebKey jsonWebKey, Context context) {
        return Mono.fromCallable(() -> decrypt(decryptParameters, jsonWebKey, context));
    }

    @Override
    public DecryptResult decrypt(DecryptParameters decryptParameters, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        return decrypt(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(), jsonWebKey,
            context);
    }

    @Override
    public Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context) {
        return serviceClientAvailable()
            ? implClient.signAsync(algorithm, digest, context)
            : Mono.error(new UnsupportedOperationException(
                "Sign operation on Local RSA key is not supported currently."));
    }

    @Override
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context) {
        if (serviceClientAvailable()) {
            return implClient.sign(algorithm, digest, context);
        } else {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("Sign operation on Local RSA key is not supported currently."));
        }
    }

    @Override
    public Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm,
        byte[] digest,
        byte[] signature,
        JsonWebKey key,
        Context context) {
        return serviceClientAvailable()
            ? implClient.verifyAsync(algorithm, digest, signature, context)
            : Mono.error(new UnsupportedOperationException(
                "Verify operation on Local RSA key is not supported currently."));
    }

    public VerifyResult verify(SignatureAlgorithm algorithm,
        byte[] digest,
        byte[] signature,
        JsonWebKey key,
        Context context) {
        if (serviceClientAvailable()) {
            return implClient.verify(algorithm, digest, signature, context);
        } else {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("Verify operation on Local RSA key is not supported currently."));
        }
    }

    @Override
    public Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, JsonWebKey jsonWebKey, Context context) {
        if (algorithm == null) {
            return Mono.error(new NullPointerException("Key wrap algorithm cannot be null."));
        }

        if (key == null) {
            return Mono.error(new NullPointerException("Key content to be wrapped cannot be null."));
        }

        keyPair = getKeyPair(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.wrapKeyAsync(algorithm, key, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return implClient.wrapKeyAsync(algorithm, key, context);
            }
            return Mono.error(new IllegalArgumentException(
                "Public portion of the key not available to perform wrap key operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            ICryptoTransform transform = algo.createEncryptor(keyPair);
            return new WrapResult(transform.doFinal(key), algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.wrapKey(algorithm, key, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return implClient.wrapKey(algorithm, key, context);
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Public portion of the key not available to perform wrap key operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createEncryptor(keyPair);
            return new WrapResult(transform.doFinal(key), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    public Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm,
        byte[] encryptedKey,
        JsonWebKey jsonWebKey,
        Context context) {
        if (algorithm == null) {
            return Mono.error(new NullPointerException("Key wrap algorithm cannot be null."));
        }

        if (encryptedKey == null) {
            return Mono.error(new NullPointerException("Encrypted key content to be unwrapped cannot be null."));
        }

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return implClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            }
            return Mono.error(new IllegalArgumentException(
                "Private portion of the key not available to perform unwrap operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            ICryptoTransform transform = algo.createDecryptor(keyPair);
            return new UnwrapResult(transform.doFinal(encryptedKey), algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm,
        byte[] encryptedKey,
        JsonWebKey jsonWebKey,
        Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return implClient.unwrapKey(algorithm, encryptedKey, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return implClient.unwrapKey(algorithm, encryptedKey, context);
            }
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Private portion of the key not available to perform unwrap operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createDecryptor(keyPair);
            return new UnwrapResult(transform.doFinal(encryptedKey), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
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
