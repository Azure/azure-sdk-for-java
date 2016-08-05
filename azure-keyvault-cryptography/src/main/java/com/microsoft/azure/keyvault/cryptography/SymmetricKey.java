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
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes128Cbc;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes128CbcHmacSha256;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes192Cbc;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes192CbcHmacSha384;
import com.microsoft.azure.keyvault.cryptography.algorithms.Aes256CbcHmacSha512;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw128;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw192;
import com.microsoft.azure.keyvault.cryptography.algorithms.AesKw256;
import com.microsoft.azure.keyvault.cryptography.Strings;

public class SymmetricKey implements IKey {

    public static final int KeySize128 = 128 >> 3;
    public static final int KeySize192 = 192 >> 3;
    public static final int KeySize256 = 256 >> 3;
    public static final int KeySize384 = 384 >> 3;
    public static final int KeySize512 = 512 >> 3;

    private final String   _kid;
    private final byte[]   _key;
    private final Provider _provider;

    public SymmetricKey(String kid, byte[] keyBytes) {
        this(kid, keyBytes, null);
    }

    public SymmetricKey(String kid, byte[] keyBytes, Provider provider) {

        if (Strings.isNullOrWhiteSpace(kid)) {
            throw new IllegalArgumentException("kid");
        }

        if (keyBytes == null) {
            throw new IllegalArgumentException("keyBytes");
        }

        if (keyBytes.length != KeySize128 && keyBytes.length != KeySize192 && keyBytes.length != KeySize256 && keyBytes.length != KeySize384 && keyBytes.length != KeySize512) {
            throw new IllegalArgumentException("The key material must be 128, 192, 256, 384 or 512 bits of data");
        }

        _kid = kid;
        _key = keyBytes;
        _provider = provider;
    }

    @Override
    public String getDefaultEncryptionAlgorithm() {

        switch (_key.length) {
        case KeySize128:
            return Aes128Cbc.AlgorithmName;

        case KeySize192:
            return Aes192Cbc.AlgorithmName;

        case KeySize256:
            return Aes128CbcHmacSha256.AlgorithmName;

        case KeySize384:
            return Aes192CbcHmacSha384.AlgorithmName;

        case KeySize512:
            return Aes256CbcHmacSha512.AlgorithmName;
        }

        return null;
    }

    @Override
    public String getDefaultKeyWrapAlgorithm() {

        switch (_key.length) {
        case KeySize128:
            return AesKw128.AlgorithmName;

        case KeySize192:
            return AesKw192.AlgorithmName;

        case KeySize256:
            return AesKw256.AlgorithmName;

        case KeySize384:
            // Default to longest allowed key length for wrap
            return AesKw256.AlgorithmName;

        case KeySize512:
            // Default to longest allowed key length for wrap
            return AesKw256.AlgorithmName;
        }

        return null;
    }

    @Override
    public String getDefaultSignatureAlgorithm() {

        return null;
    }

    @Override
    public String getKid() {

        return _kid;
    }

    @Override
    public ListenableFuture<byte[]> decryptAsync(final byte[] ciphertext, final byte[] iv, final byte[] authenticationData, final byte[] authenticationTag, final String algorithm) throws NoSuchAlgorithmException {

        if (Strings.isNullOrWhiteSpace(algorithm)) {
            throw new IllegalArgumentException("algorithm");
        }

        if (ciphertext == null) {
            throw new IllegalArgumentException("ciphertext");
        }

        if (iv == null) {
            throw new IllegalArgumentException("iv");
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);
        
        if (baseAlgorithm == null || !(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }
        
        SymmetricEncryptionAlgorithm algo = (SymmetricEncryptionAlgorithm)baseAlgorithm;

        ICryptoTransform transform = null;

        try {
            transform = algo.CreateDecryptor(_key, iv, authenticationData, _provider );
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }

        byte[] result = null;

        try {
            result = transform.doFinal(ciphertext);
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }

        if (transform instanceof IAuthenticatedCryptoTransform) {

            IAuthenticatedCryptoTransform authenticatedTransform = (IAuthenticatedCryptoTransform) transform;

            if (authenticationData == null || authenticationTag == null) {
                throw new IllegalArgumentException("AuthenticatingCryptoTransform requires authenticationData and authenticationTag");
            }

            if (!sequenceEqualConstantTime(authenticationTag, authenticatedTransform.getTag())) {
                throw new IllegalArgumentException("Data is not authentic");
            }
        }

        return Futures.immediateFuture(result);
    }

    @Override
    public ListenableFuture<Triple<byte[], byte[], String>> encryptAsync(final byte[] plaintext, final byte[] iv, final byte[] authenticationData, final String algorithm) throws NoSuchAlgorithmException {

        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext");
        }

        if (iv == null) {
            throw new IllegalArgumentException("iv");
        }

        // Interpret the algorithm
        String    algorithmName = (Strings.isNullOrWhiteSpace(algorithm)) ? getDefaultEncryptionAlgorithm() : algorithm;
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithmName);
        
        if (baseAlgorithm == null || !(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }
        
        SymmetricEncryptionAlgorithm algo = (SymmetricEncryptionAlgorithm)baseAlgorithm;

        ICryptoTransform transform = null;

        try {
            transform = algo.CreateEncryptor(_key, iv, authenticationData, _provider);
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }

        byte[] cipherText = null;

        try {
            cipherText = transform.doFinal(plaintext);
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }

        byte[] authenticationTag = null;

        if (transform instanceof IAuthenticatedCryptoTransform) {

            IAuthenticatedCryptoTransform authenticatedTransform = (IAuthenticatedCryptoTransform) transform;

            authenticationTag = authenticatedTransform.getTag().clone();
        }

        return Futures.immediateFuture(Triple.of(cipherText, authenticationTag, algorithm));
    }

    @Override
    public ListenableFuture<Pair<byte[], String>> wrapKeyAsync(final byte[] key, final String algorithm) throws NoSuchAlgorithmException {

        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("key");
        }

        // Interpret the algorithm
        String    algorithmName = (Strings.isNullOrWhiteSpace(algorithm)) ? getDefaultKeyWrapAlgorithm() : algorithm;
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithmName);
        
        if (baseAlgorithm == null || !(baseAlgorithm instanceof KeyWrapAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithmName);
        }
        
        KeyWrapAlgorithm algo = (KeyWrapAlgorithm)baseAlgorithm;

        ICryptoTransform transform = null;

        try {
            transform = algo.CreateEncryptor(_key, null, _provider);
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }

        byte[] encrypted = null;

        try {
            encrypted = transform.doFinal(key);
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }

        return Futures.immediateFuture(Pair.of(encrypted, algorithmName));
    }

    @Override
    public ListenableFuture<byte[]> unwrapKeyAsync(final byte[] encryptedKey, final String algorithm) throws NoSuchAlgorithmException {

        if (Strings.isNullOrWhiteSpace(algorithm)) {
            throw new IllegalArgumentException("algorithm");
        }

        if (encryptedKey == null || encryptedKey.length == 0) {
            throw new IllegalArgumentException("wrappedKey");
        }
        
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);
        
        if (baseAlgorithm == null || !(baseAlgorithm instanceof KeyWrapAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }
        
        KeyWrapAlgorithm algo = (KeyWrapAlgorithm)baseAlgorithm;

        ICryptoTransform transform = null;

        try {
            transform = algo.CreateDecryptor(_key, null, _provider);
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }

        byte[] decrypted = null;

        try {
            decrypted = transform.doFinal(encryptedKey);
        } catch (Exception e) {
            return Futures.immediateFailedFuture(e);
        }

        return Futures.immediateFuture(decrypted);
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
    }

    public static boolean sequenceEqualConstantTime(byte[] self, byte[] other) {
        if (self == null) {
            throw new IllegalArgumentException("self");
        }

        if (other == null) {
            throw new IllegalArgumentException("other");
        }

        // Constant time comparison of two byte arrays
        long difference = (self.length & 0xffffffffl) ^ (other.length & 0xffffffffl);

        for (int i = 0; i < self.length && i < other.length; i++) {
            difference |= (self[i] & 0xffffffffl) ^ (other[i] & 0xffffffffl);
        }

        return difference == 0;
    }

}
