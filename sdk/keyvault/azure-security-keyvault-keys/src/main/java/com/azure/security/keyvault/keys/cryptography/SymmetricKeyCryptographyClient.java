// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.azure.security.keyvault.keys.cryptography.SymmetricEncryptionAlgorithm.BLOCK_SIZE;

class SymmetricKeyCryptographyClient extends LocalKeyCryptographyClient {
    private final ClientLogger logger = new ClientLogger(SymmetricKeyCryptographyClient.class);

    private byte[] key;

    /**
     * Creates a {@link SymmetricKeyCryptographyClient} to perform local cryptography operations.
     *
     * @param serviceClient The client to route the requests through.
     */
    SymmetricKeyCryptographyClient(CryptographyServiceClient serviceClient) {
        super(serviceClient);
    }

    SymmetricKeyCryptographyClient(JsonWebKey key, CryptographyServiceClient serviceClient) {
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
    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, CryptographyOptions options,
                                     Context context, JsonWebKey jsonWebKey) {
        this.key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Key is empty."));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        byte[] iv = options.getInitializationVector();

        if (iv == null) {
            SecureRandom secureRandom = new SecureRandom();
            iv = new byte[BLOCK_SIZE];
            secureRandom.nextBytes(iv);
        }

        try {
            transform = symmetricEncryptionAlgorithm.createEncryptor(this.key, iv,
                options.getAdditionalAuthenticatedData(), null);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] encrypted;

        try {
            encrypted = transform.doFinal(plaintext);
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(new EncryptResult(encrypted, algorithm, jsonWebKey.getId()));
    }

    @Override
    Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] cipherText, CryptographyOptions options,
                                     Context context, JsonWebKey jsonWebKey) {
        this.key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Key is empty."));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        byte[] iv = options.getInitializationVector();

        if (iv == null) {
            SecureRandom secureRandom = new SecureRandom();
            iv = new byte[BLOCK_SIZE];
            secureRandom.nextBytes(iv);
        }

        try {
            transform = symmetricEncryptionAlgorithm.createDecryptor(this.key, iv,
                options.getAdditionalAuthenticatedData(), options.getTag());
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] decrypted;

        try {
            decrypted = transform.doFinal(cipherText);
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(new DecryptResult(decrypted, algorithm, jsonWebKey.getId()));
    }

    @Override
    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context, JsonWebKey key) {
        return Mono.error(new UnsupportedOperationException("Sign operation not supported for OCT/Symmetric key"));
    }

    @Override
    Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context,
                                   JsonWebKey key) {
        return Mono.error(new UnsupportedOperationException("Verify operation not supported for OCT/Symmetric key"));
    }

    @Override
    Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, CryptographyOptions options, Context context,
                                  JsonWebKey jsonWebKey) {
        this.key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("key"));
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        byte[] iv = options.getInitializationVector();

        if (iv == null) {
            SecureRandom secureRandom = new SecureRandom();
            iv = new byte[BLOCK_SIZE];
            secureRandom.nextBytes(iv);
        }

        try {
            transform = localKeyWrapAlgorithm.createEncryptor(this.key, iv, null);
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
    Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, CryptographyOptions options,
                                      Context context, JsonWebKey jsonWebKey) {
        this.key = getKey(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        byte[] iv = options.getInitializationVector();

        if (iv == null) {
            SecureRandom secureRandom = new SecureRandom();
            iv = new byte[BLOCK_SIZE];
            secureRandom.nextBytes(iv);
        }

        try {
            transform = localKeyWrapAlgorithm.createDecryptor(key, iv, null);
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
}
