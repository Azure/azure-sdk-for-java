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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

class RsaKeyCryptographyClient extends LocalKeyCryptographyClient {
    private static final ClientLogger LOGGER = new ClientLogger(RsaKeyCryptographyClient.class);

    private KeyPair keyPair;

    /*
     * Creates a RsaKeyCryptographyClient that uses {@code serviceClient) to service requests
     *
     * @param keyPair the key pair to use for cryptography operations.
     */
    RsaKeyCryptographyClient(CryptographyClientImpl serviceClient) {
        super(serviceClient);
    }

    RsaKeyCryptographyClient(JsonWebKey key, CryptographyClientImpl serviceClient) {
        super(serviceClient);

        keyPair = key.toRsa(key.hasPrivateKey());
    }

    private KeyPair getKeyPair(JsonWebKey key) {
        if (keyPair == null) {
            keyPair = key.toRsa(key.hasPrivateKey());
        }

        return keyPair;
    }

    @Override
    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey jsonWebKey,
                                     Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.encryptAsync(algorithm, plaintext, context);
            }

            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.encryptAsync(algorithm, plaintext, context);
            }

            return Mono.error(new IllegalArgumentException(
                "Public portion of the key not available to perform encrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createEncryptor(keyPair);

            return Mono.just(new EncryptResult(transform.doFinal(plaintext), algorithm,
                jsonWebKey.getId()));
        } catch (InvalidKeyException
                 | NoSuchAlgorithmException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | BadPaddingException e) {

            return Mono.error(e);
        }
    }

    @Override
    EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.encrypt(algorithm, plaintext, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.encrypt(algorithm, plaintext, context);
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Public portion of the key not available to perform encrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createEncryptor(keyPair);

            return new EncryptResult(transform.doFinal(plaintext), algorithm, jsonWebKey.getId());
        } catch (InvalidKeyException
                 | NoSuchAlgorithmException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | BadPaddingException e) {

            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    Mono<EncryptResult> encryptAsync(EncryptParameters encryptParameters, JsonWebKey jsonWebKey, Context context) {
        try {
            EncryptResult encryptResult = encrypt(encryptParameters, jsonWebKey, context);

            return Mono.just(encryptResult);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    EncryptResult encrypt(EncryptParameters encryptParameters, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        return encrypt(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(), jsonWebKey, context);
    }

    @Override
    Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] ciphertext, JsonWebKey jsonWebKey,
                                     Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.decryptAsync(algorithm, ciphertext, context);
            }

            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.decryptAsync(algorithm, ciphertext, context);
            }

            return Mono.error(new IllegalArgumentException(
                "Private portion of the key not available to perform decrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createDecryptor(keyPair);

            return Mono.just(new DecryptResult(transform.doFinal(ciphertext), algorithm,
                jsonWebKey.getId()));
        } catch (InvalidKeyException
                 | NoSuchAlgorithmException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | BadPaddingException e) {

            return Mono.error(e);
        }
    }

    @Override
    DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.decrypt(algorithm, ciphertext, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.decrypt(algorithm, ciphertext, context);
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Private portion of the key not available to perform decrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createDecryptor(keyPair);

            return new DecryptResult(transform.doFinal(ciphertext), algorithm, jsonWebKey.getId());
        } catch (InvalidKeyException
                 | NoSuchAlgorithmException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | BadPaddingException e) {

            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    Mono<DecryptResult> decryptAsync(DecryptParameters decryptParameters, JsonWebKey jsonWebKey, Context context) {
        try {
            DecryptResult decryptResult = decrypt(decryptParameters, jsonWebKey, context);

            return Mono.just(decryptResult);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    DecryptResult decrypt(DecryptParameters decryptParameters, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        return decrypt(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(), jsonWebKey,
            context);
    }

    @Override
    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context) {
        if (serviceClientAvailable()) {
            return serviceClient.signAsync(algorithm, digest, context);
        } else {
            return Mono.error(
                new UnsupportedOperationException("Sign operation on Local RSA key is not supported currently."));
        }
    }

    @Override
    SignResult sign(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context) {
        if (serviceClientAvailable()) {
            return serviceClient.sign(algorithm, digest, context);
        } else {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("Sign operation on Local RSA key is not supported currently."));
        }
    }

    @Override
    Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, JsonWebKey key,
                                   Context context) {
        if (serviceClientAvailable()) {
            return serviceClient.verifyAsync(algorithm, digest, signature, context);
        } else {
            return Mono.error(
                new UnsupportedOperationException("Verify operation on Local RSA key is not supported currently."));
        }
    }

    VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, JsonWebKey key,
                        Context context) {
        if (serviceClientAvailable()) {
            return serviceClient.verify(algorithm, digest, signature, context);
        } else {
            throw LOGGER.logExceptionAsError(
                new UnsupportedOperationException("Verify operation on Local RSA key is not supported currently."));
        }
    }

    @Override
    Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.wrapKeyAsync(algorithm, key, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.wrapKeyAsync(algorithm, key, context);
            }
            return Mono.error(new IllegalArgumentException(
                "Public portion of the key not available to perform wrap key operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createEncryptor(keyPair);
            return Mono.just(new WrapResult(transform.doFinal(key), algorithm, jsonWebKey.getId()));
        } catch (InvalidKeyException
                 | NoSuchAlgorithmException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | BadPaddingException e) {
            return Mono.error(e);
        }
    }

    @Override
    WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.wrapKey(algorithm, key, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPublic() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.wrapKey(algorithm, key, context);
            }

            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Public portion of the key not available to perform wrap key operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createEncryptor(keyPair);
            return new WrapResult(transform.doFinal(key), algorithm, jsonWebKey.getId());
        } catch (InvalidKeyException
                 | NoSuchAlgorithmException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | BadPaddingException e) {

            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, JsonWebKey jsonWebKey,
                                      Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            }
            return Mono.error(new IllegalArgumentException(
                "Private portion of the key not available to perform unwrap operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createDecryptor(keyPair);
            return Mono.just(new UnwrapResult(transform.doFinal(encryptedKey), algorithm, jsonWebKey.getId()));
        } catch (InvalidKeyException
                 | NoSuchAlgorithmException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | BadPaddingException e) {
            return Mono.error(e);
        }
    }

    @Override
    UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceClientAvailable()) {
                return serviceClient.unwrapKey(algorithm, encryptedKey, context);
            }

            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceClientAvailable()) {
                return serviceClient.unwrapKey(algorithm, encryptedKey, context);
            }
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Private portion of the key not available to perform unwrap operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createDecryptor(keyPair);
            return new UnwrapResult(transform.doFinal(encryptedKey), algorithm, jsonWebKey.getId());
        } catch (InvalidKeyException
                 | NoSuchAlgorithmException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | BadPaddingException e) {

            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
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
