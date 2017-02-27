/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.interfaces.RSAPublicKey;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.cryptography.algorithms.Rs256;
import com.microsoft.azure.keyvault.cryptography.algorithms.RsaOaep;
import com.microsoft.azure.keyvault.cryptography.Strings;

public class RsaKey implements IKey {

    public static int KeySize1024 = 1024;
    public static int KeySize2048 = 2048;

    public static int getDefaultKeySize() {
        return RsaKey.KeySize2048;
    }

    private final String   _kid;
    private final KeyPair  _keyPair;
    private final Provider _provider;

    public RsaKey(String kid) throws NoSuchAlgorithmException {
        this(kid, getDefaultKeySize());
    }

    public RsaKey(String kid, int keySize) throws NoSuchAlgorithmException {
    	this(kid, keySize, null);
    }
    
    public RsaKey(String kid, int keySize, Provider provider) throws NoSuchAlgorithmException {

        if (Strings.isNullOrWhiteSpace(kid)) {
            throw new IllegalArgumentException("kid");
        }

        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", provider);

        generator.initialize(keySize);

        _kid      = kid;
        _keyPair  = generator.generateKeyPair();
        _provider = provider;
    }

    public RsaKey(String kid, KeyPair keyPair) {
    	this(kid, keyPair, null);
    }

    public RsaKey(String kid, KeyPair keyPair, Provider provider) {

        if (Strings.isNullOrWhiteSpace(kid)) {
            throw new IllegalArgumentException("kid");
        }

        if (keyPair == null) {
            throw new IllegalArgumentException("kid");
        }

        if (keyPair.getPublic() == null || !(keyPair.getPublic() instanceof RSAPublicKey)) {
            throw new IllegalArgumentException("keyPair");
        }
        
        _kid      = kid;
        _keyPair  = keyPair;
        _provider = provider;
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
        return _kid;
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
        
        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm)baseAlgorithm;

        ICryptoTransform         transform;
        ListenableFuture<byte[]> result;

        try {
            transform = algo.CreateDecryptor(_keyPair, _provider);
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
        
        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm)baseAlgorithm;

        ICryptoTransform                                 transform;
        ListenableFuture<Triple<byte[], byte[], String>> result;

        try {
            transform = algo.CreateEncryptor(_keyPair, _provider);
            result    = Futures.immediateFuture(Triple.of(transform.doFinal(plaintext), (byte[]) null, algorithmName));
        } catch (Exception e) {
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
        
        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm)baseAlgorithm;

        ICryptoTransform                       transform;
        ListenableFuture<Pair<byte[], String>> result;

        try {
            transform = algo.CreateEncryptor(_keyPair, _provider);
            result    = Futures.immediateFuture(Pair.of(transform.doFinal(key), algorithmName));
        } catch (Exception e) {
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
        
        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm)baseAlgorithm;

        ICryptoTransform         transform;
        ListenableFuture<byte[]> result;

        try {
            transform = algo.CreateDecryptor(_keyPair, _provider);
            result    = Futures.immediateFuture(transform.doFinal(encryptedKey));
        } catch (Exception e) {
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
        
        Rs256 algo = (Rs256)baseAlgorithm;

        ISignatureTransform signer = algo.createSignatureTransform(_keyPair);
        
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
        
        Rs256 algo = (Rs256)baseAlgorithm;

        ISignatureTransform signer = algo.createSignatureTransform(_keyPair);
        
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
