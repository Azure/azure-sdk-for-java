// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.cryptography.algorithms.Rs256;
import com.microsoft.azure.keyvault.cryptography.algorithms.RsaOaep;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RsaKey implements IKey {

    public static final int KeySize1024 = 1024;
    public static final int KeySize2048 = 2048;
    public static final int KeySize4096 = 4096;

    public static int getDefaultKeySize() {
        return RsaKey.KeySize2048;
    }

    private final String   kid;
    private final KeyPair  keyPair;
    private final Provider provider;

    /**
     * Constructor.
     *
     * Generates a new RsaKey with a 2048 size keypair and a randomly generated kid.
     * @throws NoSuchAlgorithmException
     */
    public RsaKey() throws NoSuchAlgorithmException {
        this(UUID.randomUUID().toString());
    }

    /**
     * Constructor.
     *
     * Generates a new RsaKey with a 2048 size keypair and the kid given.
     * @param kid
     * @throws NoSuchAlgorithmException
     */
    public RsaKey(String kid) throws NoSuchAlgorithmException {
        this(kid, getDefaultKeySize());
    }

    /**
     * Constructor.
     *
     * Generates a new RsaKey with size keySize and the kid given.
     * @param kid
     * @param keySize
     * @throws NoSuchAlgorithmException
     */
    public RsaKey(String kid, int keySize) throws NoSuchAlgorithmException {
        this(kid, keySize, null);
    }

    /**
     * Constructor.
     *
     * Generates a new RsaKey with size keySize and the kid given. The given provider is used for algorithm implementation.
     * @param kid
     * @param keySize
     * @param provider Java security provider.
     * @throws NoSuchAlgorithmException
     */
    public RsaKey(String kid, int keySize, Provider provider) throws NoSuchAlgorithmException {

        if (Strings.isNullOrWhiteSpace(kid)) {
            throw new IllegalArgumentException("kid");
        }

        final KeyPairGenerator generator;

        generator = provider == null ?  KeyPairGenerator.getInstance("RSA") : KeyPairGenerator.getInstance("RSA", provider);

        generator.initialize(keySize);

        this.kid = kid;
        this.keyPair = generator.generateKeyPair();
        this.provider = provider;
    }

    /**
     * Constructor.
     *
     * Generates a new RsaKey with the given KeyPair.
     * The keyPair must be an RSAKey.
     * @param kid
     * @param keyPair
     */
    public RsaKey(String kid, KeyPair keyPair) {
        this(kid, keyPair, null);
    }

    /**
     * Constructor.
     *
     * Generates a new RsaKey with given KeyPair. The given provider is used for algorithm implementation.
     * The keyPair must be an RSAKey.
     * @param kid
     * @param keyPair
     * @param provider Java security provider
     */
    public RsaKey(String kid, KeyPair keyPair, Provider provider) {

        if (Strings.isNullOrWhiteSpace(kid)) {
            throw new IllegalArgumentException("Please provide a kid");
        }

        if (keyPair == null) {
            throw new IllegalArgumentException("Please provide a KeyPair");
        }

        if (keyPair.getPublic() == null || !(keyPair.getPublic() instanceof RSAPublicKey)) {
            throw new IllegalArgumentException("The KeyPair is not an RsaKey");
        }

        this.kid = kid;
        this.keyPair = keyPair;
        this.provider = provider;
    }

    /**
     * Converts JSON web key to RsaKey.
     * @param jwk
     * @return RsaKey
     */
    public static RsaKey fromJsonWebKey(JsonWebKey jwk) {
        return fromJsonWebKey(jwk, false, null);
    }

    /**
     * Converts JSON web key to RsaKey and include the private key if set to true.
     * @param jwk
     * @param includePrivateParameters true if the RSA key pair should include the private key. False otherwise.
     * @return RsaKey
     */
    public static RsaKey fromJsonWebKey(JsonWebKey jwk, boolean includePrivateParameters) {
        return fromJsonWebKey(jwk, includePrivateParameters, null);
    }

    /**
     * Converts JSON web key to RsaKey and include the private key if set to true.
     * @param provider the Java security provider.
     * @param includePrivateParameters true if the RSA key pair should include the private key. False otherwise.
     * @return RsaKey
     */
    public static RsaKey fromJsonWebKey(JsonWebKey jwk, boolean includePrivateParameters, Provider provider) {
        if (jwk.kid() != null) {
            return new RsaKey(jwk.kid(), jwk.toRSA(includePrivateParameters, provider));
        } else {
            throw new IllegalArgumentException("Json Web Key must have a kid");
        }
    }

    /**
     * Converts RsaKey to JSON web key.
     * @return
     */
    public JsonWebKey toJsonWebKey() {
        return JsonWebKey.fromRSA(keyPair);
    }

    @Override
    public String getDefaultEncryptionAlgorithm() {
        return RsaOaep.ALGORITHM_NAME;
    }

    @Override
    public String getDefaultKeyWrapAlgorithm() {
        return RsaOaep.ALGORITHM_NAME;
    }

    @Override
    public String getDefaultSignatureAlgorithm() {
        return Rs256.ALGORITHM_NAME;
    }

    @Override
    public String getKid() {
        return kid;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    @Override
    public ListenableFuture<byte[]> decryptAsync(final byte[] ciphertext, final byte[] iv, final byte[] authenticationData, final byte[] authenticationTag, final String algorithm) throws NoSuchAlgorithmException {

        if (ciphertext == null) {
            throw new IllegalArgumentException("ciphertext");
        }

        // Interpret the requested algorithm
        if (Strings.isNullOrWhiteSpace(algorithm)) {
            throw new IllegalArgumentException("algorithm");
        }

        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);

        if (baseAlgorithm == null || !(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform         transform;
        ListenableFuture<byte[]> result;

        try {
            transform = algo.CreateDecryptor(keyPair, provider);
            result    = Futures.immediateFuture(transform.doFinal(ciphertext));
        } catch (Exception e) {
            result    = Futures.immediateFailedFuture(e);
        }

        return result;
    }

    @Override
    public ListenableFuture<Triple<byte[], byte[], String>> encryptAsync(final byte[] plaintext, final byte[] iv, final byte[] authenticationData, final String algorithm) throws NoSuchAlgorithmException {

        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext");
        }

        // Interpret the requested algorithm
        String    algorithmName = (Strings.isNullOrWhiteSpace(algorithm) ? getDefaultEncryptionAlgorithm() : algorithm);
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithmName);

        if (baseAlgorithm == null || !(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithmName);
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform                                 transform;
        ListenableFuture<Triple<byte[], byte[], String>> result;

        try {
            transform = algo.CreateEncryptor(keyPair, provider);
            result    = Futures.immediateFuture(Triple.of(transform.doFinal(plaintext), (byte[]) null, algorithmName));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            result    = Futures.immediateFailedFuture(e);
        }

        return result;
    }

    @Override
    public ListenableFuture<Pair<byte[], String>> wrapKeyAsync(final byte[] key, final String algorithm) throws NoSuchAlgorithmException {

        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        // Interpret the requested algorithm
        String    algorithmName = (Strings.isNullOrWhiteSpace(algorithm) ? getDefaultKeyWrapAlgorithm() : algorithm);
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithmName);

        if (baseAlgorithm == null || !(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithmName);
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform                       transform;
        ListenableFuture<Pair<byte[], String>> result;

        try {
            transform = algo.CreateEncryptor(keyPair, provider);
            result    = Futures.immediateFuture(Pair.of(transform.doFinal(key), algorithmName));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            result    = Futures.immediateFailedFuture(e);
        }

        return result;
    }

    @Override
    public ListenableFuture<byte[]> unwrapKeyAsync(final byte[] encryptedKey, final String algorithm) throws NoSuchAlgorithmException {

        if (encryptedKey == null) {
            throw new IllegalArgumentException("encryptedKey ");
        }

        // Interpret the requested algorithm
        if (Strings.isNullOrWhiteSpace(algorithm)) {
            throw new IllegalArgumentException("algorithm");
        }

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);

        if (baseAlgorithm == null || !(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform         transform;
        ListenableFuture<byte[]> result;

        try {
            transform = algo.CreateDecryptor(keyPair, provider);
            result    = Futures.immediateFuture(transform.doFinal(encryptedKey));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            result    = Futures.immediateFailedFuture(e);
        }

        return result;
    }

    @Override
    public ListenableFuture<Pair<byte[], String>> signAsync(final byte[] digest, final String algorithm) throws NoSuchAlgorithmException {

        if (digest == null) {
            throw new IllegalArgumentException("encryptedKey ");
        }

        // Interpret the requested algorithm
        if (Strings.isNullOrWhiteSpace(algorithm)) {
            throw new IllegalArgumentException("algorithm");
        }

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);

        if (baseAlgorithm == null || !(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }

        Rs256 algo = (Rs256) baseAlgorithm;

        ISignatureTransform signer = algo.createSignatureTransform(keyPair);

        try {
            return Futures.immediateFuture(Pair.of(signer.sign(digest), Rs256.ALGORITHM_NAME));
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }
    }

    @Override
    public ListenableFuture<Boolean> verifyAsync(final byte[] digest, final byte[] signature, final String algorithm) throws NoSuchAlgorithmException {

        if (digest == null) {
            throw new IllegalArgumentException("encryptedKey ");
        }

        // Interpret the requested algorithm
        if (Strings.isNullOrWhiteSpace(algorithm)) {
            throw new IllegalArgumentException("algorithm");
        }

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);

        if (baseAlgorithm == null || !(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }

        Rs256 algo = (Rs256) baseAlgorithm;

        ISignatureTransform signer = algo.createSignatureTransform(keyPair);

        try {
            return Futures.immediateFuture(signer.verify(digest, signature));
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }
    }

    @Override
    public void close() throws IOException {
        // Intentionally empty
    }

}
