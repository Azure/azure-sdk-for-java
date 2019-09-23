// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.keyvault.cryptography.Algorithm;
import com.microsoft.azure.keyvault.cryptography.AlgorithmResolver;
import com.microsoft.azure.keyvault.cryptography.ByteExtensions;
import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;
import com.microsoft.azure.keyvault.cryptography.KeyWrapAlgorithm;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw128;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw192;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw256;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.security.Provider;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * A simple symmetric key implementation
 *
 */
// TODO: Remove file in favor of actual keyvault dependency
public class SymmetricKey implements IKey {

    private final ClientLogger logger = new ClientLogger(SymmetricKey.class);

    private static final SecureRandom Rng = new SecureRandom();

    public static final int KeySize128 = 128 >> 3;
    public static final int KeySize192 = 192 >> 3;
    public static final int KeySize256 = 256 >> 3;
    public static final int KeySize384 = 384 >> 3;
    public static final int KeySize512 = 512 >> 3;

    public static final int DefaultKeySize = KeySize256;

    private final String   kid;
    private final byte[]   key;
    private final Provider provider;

    /**
     * Creates a SymmetricKey with a random key identifier and
     * a random key with DefaultKeySize bits.
     */
    public SymmetricKey() {
        this(UUID.randomUUID().toString());
    }

    /**
     * Creates a SymmetricKey with the specified key identifier and
     * a random key with DefaultKeySize bits.
     * @param kid The key identifier to use.
     */
    public SymmetricKey(String kid) {
        this(kid, DefaultKeySize);
    }

    /**
     * Creates a SymmetricKey with the specified key identifier and
     * a random key with the specified size.
     * @param kid The key identifier to use.
     * @param keySizeInBytes The key size to use in bytes.
     */
    public SymmetricKey(String kid, int keySizeInBytes) {
        this(kid, keySizeInBytes, null);
    }

    /**
     * Creates a SymmetricKey with the specified key identifier and
     * a random key with the specified size that uses the specified provider.
     * @param kid The key identifier to use.
     * @param keySizeInBytes The key size to use in bytes.
     * @param provider The provider to use (optional, null for default)
     * @throws IllegalArgumentException for bad key
     */
    public SymmetricKey(String kid, int keySizeInBytes, Provider provider) {

        if (keySizeInBytes != KeySize128 && keySizeInBytes != KeySize192 && keySizeInBytes
            != KeySize256 && keySizeInBytes != KeySize384 && keySizeInBytes != KeySize512) {
            // (gapra) temp fix until we get actual dependencies
            throw logger.logExceptionAsError(new IllegalArgumentException("The key material "
                + "must be 128, 192, 256, 384 or 512 bits of data"));
        }

        this.kid      = kid;
        this.key      = new byte[keySizeInBytes];
        this.provider = provider;

        // Generate a random key
        Rng.nextBytes(key);
    }

    /**
     * Creates a SymmetricKey with the specified key identifier and key material.
     * @param kid The key identifier to use.
     * @param keyBytes The key material to use.
     */
    public SymmetricKey(String kid, byte[] keyBytes) {
        this(kid, keyBytes, null);
    }

    /**
     * Creates a SymmetricKey with the specified key identifier and key material
     * that uses the specified Provider.
     * @param kid The key identifier to use.
     * @param keyBytes The key material to use.
     * @param provider The Provider to use (optional, null for default)
     * @throws IllegalArgumentException for bad key
     */
    public SymmetricKey(String kid, byte[] keyBytes, Provider provider) {

        if (keyBytes == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("keyBytes"));
        }

        if (keyBytes.length != KeySize128 && keyBytes.length != KeySize192 && keyBytes.length
            != KeySize256 && keyBytes.length != KeySize384 && keyBytes.length != KeySize512) {
            // (gapra) temp fix until we get actual dependencies
            throw logger.logExceptionAsError(new IllegalArgumentException("The key material "
                + "must be 128, 192, 256, 384 or 512 bits of data"));
        }

        this.kid      = kid;
        this.key      = ByteExtensions.clone(keyBytes);
        this.provider = provider;
    }

    String getDefaultKeyWrapAlgorithm() {

        switch (key.length) {
            case KeySize128:
                return AesKw128.ALGORITHM_NAME;

            case KeySize192:
                return AesKw192.ALGORITHM_NAME;

            case KeySize256:
                return AesKw256.ALGORITHM_NAME;

            case KeySize384:
                // Default to longest allowed key length for wrap
                return AesKw256.ALGORITHM_NAME;

            case KeySize512:
                // Default to longest allowed key length for wrap
                return AesKw256.ALGORITHM_NAME;

            default:
                return null;
        }
    }

    @Override
    public String getKid() {
        return kid;
    }

    @Override
    public Mono<Tuple2<byte[], String>> wrapKeyAsync(final byte[] key, String algorithm) throws RuntimeException {

        if (key == null || key.length == 0) {
            // (gapra) temp fix until we get actual dependencies
            throw logger.logExceptionAsError(new IllegalArgumentException("key"));
        }

        if (algorithm == null || algorithm.isEmpty()) {
            algorithm = getDefaultKeyWrapAlgorithm();
        }
        // Interpret the algorithm
        String    algorithmName = algorithm;
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithmName);

        if (baseAlgorithm == null || !(baseAlgorithm instanceof KeyWrapAlgorithm)) {
            // (gapra) temp fix until we get actual dependencies
            throw logger.logExceptionAsError(new RuntimeException("No such algorithm: " + algorithmName));
        }

        KeyWrapAlgorithm algo = (KeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform = null;

        try {
            transform = algo.CreateEncryptor(this.key, null, provider);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] encrypted = null;

        try {
            encrypted = transform.doFinal(key);
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(encrypted).zipWith(Mono.just(algorithmName));
    }

    @Override
    public Mono<byte[]> unwrapKeyAsync(final byte[] encryptedKey, final String algorithm) throws RuntimeException {

        if (encryptedKey == null || encryptedKey.length == 0) {
            // (gapra) temp fix until we get actual dependencies
            throw logger.logExceptionAsError(new IllegalArgumentException("wrappedKey"));
        }

        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);

        if (baseAlgorithm == null || !(baseAlgorithm instanceof KeyWrapAlgorithm)) {
            // (gapra) temp fix until we get actual dependencies
            throw logger.logExceptionAsError(new RuntimeException("No such algorithm: " + algorithm));
        }

        KeyWrapAlgorithm algo = (KeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform = null;

        try {
            transform = algo.CreateDecryptor(key, null, provider);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] decrypted = null;

        try {
            decrypted = transform.doFinal(encryptedKey);
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(decrypted);
    }
}
