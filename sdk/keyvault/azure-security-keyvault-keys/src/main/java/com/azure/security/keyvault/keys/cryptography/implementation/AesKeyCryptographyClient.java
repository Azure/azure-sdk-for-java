// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

class AesKeyCryptographyClient extends LocalKeyCryptographyClient {
    private static final ClientLogger LOGGER = new ClientLogger(AesKeyCryptographyClient.class);

    private byte[] key;

    static final int AES_BLOCK_SIZE = 16;

    AesKeyCryptographyClient(JsonWebKey key, CryptographyClientImpl implClient) {
        super(implClient);
        this.key = key.toAes().getEncoded();
    }

    private byte[] getKey(JsonWebKey key) {
        if (this.key == null) {
            this.key = key.toAes().getEncoded();
        }
        return this.key;
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

        try {
            return encryptInternalAsync(algorithm, plaintext, null, null, jsonWebKey, context);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    public EncryptResult encrypt(EncryptionAlgorithm algorithm,
        byte[] plaintext,
        JsonWebKey jsonWebKey,
        Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        return encryptInternal(algorithm, plaintext, null, null, jsonWebKey, context);
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptParameters encryptParameters, JsonWebKey jsonWebKey, Context context) {
        if (encryptParameters == null) {
            return Mono.error(new NullPointerException("Encrypt parameters cannot be null."));
        }

        try {
            return encryptInternalAsync(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(),
                encryptParameters.getIv(), encryptParameters.getAdditionalAuthenticatedData(), jsonWebKey, context);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    public EncryptResult encrypt(EncryptParameters encryptParameters, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        return encryptInternal(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(),
            encryptParameters.getIv(), encryptParameters.getAdditionalAuthenticatedData(), jsonWebKey, context);
    }

    private Mono<EncryptResult> encryptInternalAsync(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv,
                                                     byte[] additionalAuthenticatedData, JsonWebKey jsonWebKey,
                                                     Context context) {
        RuntimeException validationAlgorithm = validateEncryptionAlgorithm(algorithm);
        if (validationAlgorithm != null) {
            return Mono.error(validationAlgorithm);
        }

        key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            return Mono.error(new IllegalArgumentException("Key is empty."));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        final byte[] finalIv;
        if (iv == null) {
            if (isAes(algorithm)) {
                finalIv = generateRandomByteArray(AES_BLOCK_SIZE);
            } else {
                return Mono.error(
                    new IllegalStateException("Encryption algorithm provided is not supported: " + algorithm));
            }
        } else {
            finalIv = iv;
        }

        return Mono.fromCallable(() -> {
            byte[] ciphertext = symmetricEncryptionAlgorithm.createEncryptor(key, finalIv, additionalAuthenticatedData,
                null)
                .doFinal(plaintext);

            return new EncryptResult(ciphertext, algorithm, jsonWebKey.getId(), finalIv, null,
                additionalAuthenticatedData);
        });
    }

    private EncryptResult encryptInternal(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv,
                                          byte[] additionalAuthenticatedData, JsonWebKey jsonWebKey,
                                          Context context) {
        RuntimeException validationAlgorithm = validateEncryptionAlgorithm(algorithm);
        if (validationAlgorithm != null) {
            throw LOGGER.logExceptionAsError(validationAlgorithm);
        }

        key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Key is empty."));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        if (iv == null) {
            if (isAes(algorithm)) {
                iv = generateRandomByteArray(AES_BLOCK_SIZE);
            } else {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Encryption algorithm provided is not supported: " + algorithm));
            }
        }

        try {
            byte[] ciphertext = symmetricEncryptionAlgorithm.createEncryptor(key, iv, additionalAuthenticatedData,
                null)
                .doFinal(plaintext);

            return new EncryptResult(ciphertext, algorithm, jsonWebKey.getId(), iv, null,
                additionalAuthenticatedData);
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
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

        try {
            return decryptInternalAsync(algorithm, ciphertext, null, null, null, jsonWebKey, context);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    public DecryptResult decrypt(EncryptionAlgorithm algorithm,
        byte[] ciphertext,
        JsonWebKey jsonWebKey,
        Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        return decryptInternal(algorithm, ciphertext, null, null, null, jsonWebKey, context);
    }

    @Override
    public Mono<DecryptResult> decryptAsync(DecryptParameters decryptParameters, JsonWebKey jsonWebKey, Context context) {
        try {
            return decryptInternalAsync(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(),
                decryptParameters.getIv(), decryptParameters.getAdditionalAuthenticatedData(),
                decryptParameters.getAuthenticationTag(), jsonWebKey, context);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    public DecryptResult decrypt(DecryptParameters decryptParameters, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        return decryptInternal(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(),
            decryptParameters.getIv(), decryptParameters.getAdditionalAuthenticatedData(),
            decryptParameters.getAuthenticationTag(), jsonWebKey, context);
    }

    private Mono<DecryptResult> decryptInternalAsync(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv,
                                                     byte[] additionalAuthenticatedData, byte[] authenticationTag,
                                                     JsonWebKey jsonWebKey, Context context) {
        RuntimeException validationAlgorithm = validateEncryptionAlgorithm(algorithm);
        if (validationAlgorithm != null) {
            return Mono.error(validationAlgorithm);
        }

        key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            return Mono.error(new IllegalArgumentException("Key is empty."));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        if (iv == null) {
            return Mono.error(new NullPointerException("'iv' cannot be null in local decryption operations."));
        }

        return Mono.fromCallable(() -> {
            byte[] plaintext = symmetricEncryptionAlgorithm.createDecryptor(key, iv, additionalAuthenticatedData,
                authenticationTag)
                .doFinal(ciphertext);

            return new DecryptResult(plaintext, algorithm, jsonWebKey.getId());
        });
    }

    private DecryptResult decryptInternal(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv,
                                          byte[] additionalAuthenticatedData, byte[] authenticationTag,
                                          JsonWebKey jsonWebKey, Context context) {
        RuntimeException algorithmValidation = validateEncryptionAlgorithm(algorithm);
        if (algorithmValidation != null) {
            throw LOGGER.logExceptionAsError(algorithmValidation);
        }

        key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Key is empty."));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        Objects.requireNonNull(iv, "'iv' cannot be null in local decryption operations.");

        try {
            byte[] plaintext = symmetricEncryptionAlgorithm.createDecryptor(key, iv, additionalAuthenticatedData,
                authenticationTag)
                .doFinal(ciphertext);

            return new DecryptResult(plaintext, algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private static RuntimeException validateEncryptionAlgorithm(EncryptionAlgorithm algorithm) {
        if (isGcm(algorithm)) {
            return new UnsupportedOperationException("AES-GCM is not supported for local cryptography operations.");
        }

        if (!isAes(algorithm)) {
            return new IllegalStateException("Encryption algorithm provided is not supported: " + algorithm);
        }

        return null;
    }

    @Override
    public Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context) {
        return Mono.error(new UnsupportedOperationException("Sign operation not supported for OCT/Symmetric key."));
    }

    @Override
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Sign operation not supported for OCT/Symmetric key."));
    }

    @Override
    public Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm,
        byte[] digest,
        byte[] signature,
        JsonWebKey key,
        Context context) {
        return Mono.error(new UnsupportedOperationException("Verify operation not supported for OCT/Symmetric key."));
    }

    public VerifyResult verify(SignatureAlgorithm algorithm,
        byte[] digest,
        byte[] signature,
        JsonWebKey key,
        Context context) {
        throw LOGGER.logExceptionAsError(
            new UnsupportedOperationException("Verify operation not supported for OCT/Symmetric key."));
    }

    @Override
    public Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm,
        byte[] keyToWrap,
        JsonWebKey jsonWebKey,
        Context context) {
        if (algorithm == null) {
            return Mono.error(new NullPointerException("Key wrap algorithm cannot be null."));
        }

        if (keyToWrap == null) {
            return Mono.error(new NullPointerException("Key content to be wrapped cannot be null."));
        }

        key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            return FluxUtil.monoError(LOGGER, new IllegalArgumentException("key"));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            byte[] encrypted = localKeyWrapAlgorithm.createEncryptor(key, null, null)
                .doFinal(keyToWrap);

            return new WrapResult(encrypted, algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] keyToWrap, JsonWebKey jsonWebKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(keyToWrap, "Key content to be wrapped cannot be null.");

        this.key = getKey(jsonWebKey);

        if (this.key == null || this.key.length == 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("key"));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = localKeyWrapAlgorithm.createEncryptor(this.key, null, null);
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }

        byte[] encrypted;

        try {
            encrypted = transform.doFinal(keyToWrap);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw LOGGER.logExceptionAsError((RuntimeException) e);
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        return new WrapResult(encrypted, algorithm, jsonWebKey.getId());
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

        this.key = getKey(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            byte[] decrypted = localKeyWrapAlgorithm.createDecryptor(this.key, null, null)
                .doFinal(encryptedKey);

            return new UnwrapResult(decrypted, algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm,
        byte[] encryptedKey,
        JsonWebKey jsonWebKey,
        Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        this.key = getKey(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            throw LOGGER.logExceptionAsError(new RuntimeException(new NoSuchAlgorithmException(algorithm.toString())));
        }

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        try {
            byte[] decrypted = localKeyWrapAlgorithm.createDecryptor(this.key, null, null)
                .doFinal(encryptedKey);

            return new UnwrapResult(decrypted, algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    public Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, JsonWebKey key, Context context) {
        return signAsync(algorithm, data, key, context);
    }

    @Override
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, JsonWebKey key, Context context) {
        return sign(algorithm, data, key, context);
    }

    @Override
    public Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm,
        byte[] data,
        byte[] signature,
        JsonWebKey key,
        Context context) {
        return verifyAsync(algorithm, data, signature, key, context);
    }

    public VerifyResult verifyData(SignatureAlgorithm algorithm,
        byte[] data,
        byte[] signature,
        JsonWebKey key,
        Context context) {
        return verify(algorithm, data, signature, key, context);
    }

    private static byte[] generateRandomByteArray(int sizeInBytes) {
        byte[] iv = new byte[0];

        try {
            SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
            iv = new byte[sizeInBytes];
            randomSecureRandom.nextBytes(iv);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.logThrowableAsError(e);
        }

        return iv;
    }

    private static boolean isAes(EncryptionAlgorithm encryptionAlgorithm) {
        return (encryptionAlgorithm == EncryptionAlgorithm.A128CBC
            || encryptionAlgorithm == EncryptionAlgorithm.A192CBC
            || encryptionAlgorithm == EncryptionAlgorithm.A256CBC
            || encryptionAlgorithm == EncryptionAlgorithm.A128CBCPAD
            || encryptionAlgorithm == EncryptionAlgorithm.A192CBCPAD
            || encryptionAlgorithm == EncryptionAlgorithm.A256CBCPAD);
    }

    private static boolean isGcm(EncryptionAlgorithm encryptionAlgorithm) {
        return (encryptionAlgorithm == EncryptionAlgorithm.A128GCM
            || encryptionAlgorithm == EncryptionAlgorithm.A192GCM
            || encryptionAlgorithm == EncryptionAlgorithm.A256GCM);
    }
}
