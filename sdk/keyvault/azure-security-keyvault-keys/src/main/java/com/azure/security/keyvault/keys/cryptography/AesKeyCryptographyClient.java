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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

class AesKeyCryptographyClient extends LocalKeyCryptographyClient {
    static final int AES_BLOCK_SIZE = 16;

    private final ClientLogger logger = new ClientLogger(AesKeyCryptographyClient.class);

    private byte[] key;

    /**
     * Creates a {@link AesKeyCryptographyClient} to perform local cryptography operations.
     *
     * @param serviceClient The client to route the requests through.
     */
    AesKeyCryptographyClient(CryptographyServiceClient serviceClient) {
        super(serviceClient);
    }

    AesKeyCryptographyClient(JsonWebKey key, CryptographyServiceClient serviceClient) {
        super(serviceClient);
        this.key = key.toAes().getEncoded();
    }

    private byte[] getKey(JsonWebKey key) {
        if (this.key == null) {
            this.key = key.toAes().getEncoded();
        }
        return this.key;
    }



    @Override
    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context,
                                     JsonWebKey jsonWebKey) {
        return encryptInternal(algorithm, plaintext, null, null, context, jsonWebKey);
    }

    @Override
    Mono<EncryptResult> encryptAsync(EncryptParameters encryptParameters, Context context, JsonWebKey jsonWebKey) {
        return encryptInternal(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(),
            encryptParameters.getIv(), encryptParameters.getAdditionalAuthenticatedData(), context, jsonWebKey);
    }

    private Mono<EncryptResult> encryptInternal(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv,
                                                byte[] additionalAuthenticatedData, Context context,
                                                JsonWebKey jsonWebKey) {
        if (isGcm(algorithm)) {
            return Mono.error(
                new UnsupportedOperationException("AES-GCM is not supported for local cryptography operations."));
        }

        if (!isAes(algorithm)) {
            throw logger.logExceptionAsError(
                new IllegalStateException("Encryption algorithm provided is not supported: " + algorithm));
        }

        this.key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Key is empty."));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        if (iv == null) {
            if (isAes(algorithm)) {
                iv = generateRandomByteArray(AES_BLOCK_SIZE);
            } else {
                throw logger.logExceptionAsError(
                    new IllegalStateException("Encryption algorithm provided is not supported: " + algorithm));
            }
        }

        try {
            transform = symmetricEncryptionAlgorithm.createEncryptor(this.key, iv, additionalAuthenticatedData,
                null);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] ciphertext;

        try {
            ciphertext = transform.doFinal(plaintext);
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(new EncryptResult(ciphertext, algorithm, jsonWebKey.getId(), iv, null,
            additionalAuthenticatedData));
    }

    @Override
    Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context,
                                     JsonWebKey jsonWebKey) {
        return decryptInternal(algorithm, ciphertext, null, null, null, context, jsonWebKey);
    }

    @Override
    Mono<DecryptResult> decryptAsync(DecryptParameters decryptParameters, Context context, JsonWebKey jsonWebKey) {
        return decryptInternal(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(),
            decryptParameters.getIv(), decryptParameters.getAdditionalAuthenticatedData(),
            decryptParameters.getAuthenticationTag(), context, jsonWebKey);
    }

    private Mono<DecryptResult> decryptInternal(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv,
                                                byte[] additionalAuthenticatedData, byte[] authenticationTag,
                                                Context context, JsonWebKey jsonWebKey) {
        if (isGcm(algorithm)) {
            return Mono.error(
                new UnsupportedOperationException("AES-GCM is not supported for local cryptography operations."));
        }

        if (!isAes(algorithm)) {
            throw logger.logExceptionAsError(
                new IllegalStateException("Encryption algorithm provided is not supported: " + algorithm));
        }

        this.key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Key is empty."));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        Objects.requireNonNull(iv, "'iv' cannot be null in local decryption operations.");

        try {
            transform = symmetricEncryptionAlgorithm.createDecryptor(this.key, iv, additionalAuthenticatedData,
                authenticationTag);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] plaintext;

        try {
            plaintext = transform.doFinal(ciphertext);
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(new DecryptResult(plaintext, algorithm, jsonWebKey.getId()));
    }

    @Override
    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context, JsonWebKey key) {
        return Mono.error(new UnsupportedOperationException("Sign operation not supported for OCT/Symmetric key."));
    }

    @Override
    Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context,
                                   JsonWebKey key) {
        return Mono.error(new UnsupportedOperationException("Verify operation not supported for OCT/Symmetric key."));
    }

    @Override
    Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context, JsonWebKey jsonWebKey) {
        this.key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("key"));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = localKeyWrapAlgorithm.createEncryptor(this.key, null, null);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] encrypted;

        try {
            encrypted = transform.doFinal(key);
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(new WrapResult(encrypted, algorithm, jsonWebKey.getId()));
    }

    @Override
    Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context, JsonWebKey jsonWebKey) {
        this.key = getKey(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = localKeyWrapAlgorithm.createDecryptor(this.key, null, null);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] decrypted;

        try {
            decrypted = transform.doFinal(encryptedKey);
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(new UnwrapResult(decrypted, algorithm, jsonWebKey.getId()));
    }

    @Override
    Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, Context context, JsonWebKey key) {
        return signAsync(algorithm, data, context, key);
    }

    @Override
    Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context,
                                       JsonWebKey key) {
        return verifyAsync(algorithm, data, signature, context, key);
    }

    private byte[] generateRandomByteArray(int sizeInBytes) {
        byte[] iv = new byte[0];
        SecureRandom randomSecureRandom;

        try {
            randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
            iv = new byte[sizeInBytes];
            randomSecureRandom.nextBytes(iv);
        } catch (NoSuchAlgorithmException e) {
            logger.logThrowableAsError(e);
        }

        return iv;
    }

    private boolean isAes(EncryptionAlgorithm encryptionAlgorithm) {
        return (encryptionAlgorithm == EncryptionAlgorithm.A128CBC
            || encryptionAlgorithm == EncryptionAlgorithm.A192CBC
            || encryptionAlgorithm == EncryptionAlgorithm.A256CBC
            || encryptionAlgorithm == EncryptionAlgorithm.A128CBCPAD
            || encryptionAlgorithm == EncryptionAlgorithm.A192CBCPAD
            || encryptionAlgorithm == EncryptionAlgorithm.A256CBCPAD);
    }

    private boolean isGcm(EncryptionAlgorithm encryptionAlgorithm) {
        return (encryptionAlgorithm == EncryptionAlgorithm.A128GCM
            || encryptionAlgorithm == EncryptionAlgorithm.A192GCM
            || encryptionAlgorithm == EncryptionAlgorithm.A256GCM);
    }
}
