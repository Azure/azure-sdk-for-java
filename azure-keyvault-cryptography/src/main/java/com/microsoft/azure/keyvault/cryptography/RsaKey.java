/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.cryptography;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.cryptography.algorithms.RsaOaep;
import com.microsoft.azure.keyvault.cryptography.Strings;

public class RsaKey implements IKey {

    public static int KeySize1024 = 1024;
    public static int KeySize2048 = 2048;

    public static int getDefaultKeySize() {
        return RsaKey.KeySize2048;
    }

    private final String  _kid;
    private final KeyPair _keyPair;

    public RsaKey(String kid) throws NoSuchAlgorithmException {
        this(kid, getDefaultKeySize());
    }

    public RsaKey(String kid, int keySize) throws NoSuchAlgorithmException {

        if (Strings.isNullOrWhiteSpace(kid)) {
            throw new IllegalArgumentException("kid");
        }

        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(keySize);

        _keyPair = generator.generateKeyPair();
        _kid = kid;
    }

    public RsaKey(String kid, KeyPair keyPair) {

        if (Strings.isNullOrWhiteSpace(kid)) {
            throw new IllegalArgumentException("kid");
        }

        if (keyPair == null) {
            throw new IllegalArgumentException("kid");
        }

        if (keyPair.getPublic() == null || !(keyPair.getPublic() instanceof RSAPublicKey)) {
            throw new IllegalArgumentException("keyPair");
        }

        _keyPair = keyPair;
        _kid = kid;
    }

    @Override
    public String getDefaultEncryptionAlgorithm() {
        return RsaOaep.AlgorithmName;
    }

    @Override
    public String getDefaultKeyWrapAlgorithm() {
        return RsaOaep.AlgorithmName;
    }

    @Override
    public String getDefaultSignatureAlgorithm() {
    	// TODO: Signature Processing
        return null;
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
            transform = algo.CreateDecryptor(_keyPair);
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
            transform = algo.CreateEncryptor(_keyPair);
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
            transform = algo.CreateEncryptor(_keyPair);
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
            transform = algo.CreateDecryptor(_keyPair);
            result    = Futures.immediateFuture(transform.doFinal(encryptedKey));
        } catch (Exception e) {
            result    = Futures.immediateFailedFuture(e);
        }

        return result;
    }

    @Override
    public ListenableFuture<Pair<byte[], String>> signAsync(final byte[] digest, final String algorithm) {
        return Futures.immediateFailedFuture(new NotImplementedException("signAsync is not currently supported"));
    }

    @Override
    public ListenableFuture<Boolean> verifyAsync(final byte[] digest, final byte[] signature, final String algorithm) {
        return Futures.immediateFailedFuture(new NotImplementedException("verifyAsync is not currently supported"));
    }

    @Override
    public void close() throws IOException {
        // Intentionally empty
    }

}
