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

package com.microsoft.azure.keyvault.extensions;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.cryptography.RsaKey;
import com.microsoft.azure.keyvault.cryptography.Strings;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.models.JsonWebKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;
import com.microsoft.rest.ServiceResponse;

/**
 * The key vault key that performs cryptography operations.
 */
public class KeyVaultKey implements IKey {
    
    /**
     * Transforms the result of decrypt operation to byte array.
     */
    class DecryptResultTransform implements Function<ServiceResponse<KeyOperationResult>, byte[]> {

        DecryptResultTransform() {
            super();
        }

        @Override
        public byte[] apply(ServiceResponse<KeyOperationResult> result) {
            return result.getBody().result();
        }
    }

    /**
     * Transforms the result of sign operation to byte array and algorithm pair.
     */
    class SignResultTransform implements Function<ServiceResponse<KeyOperationResult>, Pair<byte[], String>> {

        private final String algorithm;

        SignResultTransform(String algorithm) {
            super();
            this.algorithm = algorithm;
        }
        
        @Override
        public Pair<byte[], String> apply(ServiceResponse<KeyOperationResult> input) {

            return Pair.of(input.getBody().result(), algorithm);
        }
    }

    private final KeyVaultClient client;
    private IKey                 implementation;

    protected KeyVaultKey(KeyVaultClient client, KeyBundle keyBundle) {

        if (client == null) {
            throw new IllegalArgumentException("client");
        }

        if (keyBundle == null) {
            throw new IllegalArgumentException("keyBundle");
        }

        JsonWebKey key = keyBundle.key();

        if (key == null) {
            throw new IllegalArgumentException("keyBundle must contain a key");
        }

        if (key.kty().equals(JsonWebKeyType.RSA)) {
            // The private key is not available for KeyVault keys
            implementation = new RsaKey(key.kid(), key.toRSA(false));
        } else if (key.kty().equals(JsonWebKeyType.RSAHSM)) {
            // The private key is not available for KeyVault keys
            implementation = new RsaKey(key.kid(), key.toRSA(false));
        }

        if (implementation == null) {
            throw new IllegalArgumentException(String.format("The key type %s is not supported", key.kty()));
        }

        this.client = client;
    }

    @Override
    public void close() throws IOException {
        if (implementation != null) {
            implementation.close();
        }
    }

    @Override
    public String getDefaultEncryptionAlgorithm() {
        if (implementation == null) {
            return null;
        }

        return implementation.getDefaultEncryptionAlgorithm();
    }

    @Override
    public String getDefaultKeyWrapAlgorithm() {

        if (implementation == null) {
            return null;
        }

        return implementation.getDefaultKeyWrapAlgorithm();
    }

    @Override
    public String getDefaultSignatureAlgorithm() {

        if (implementation == null) {
            return null;
        }

        return implementation.getDefaultSignatureAlgorithm();
    }

    @Override
    public String getKid() {

        if (implementation == null) {
            return null;
        }

        return implementation.getKid();
    }

    @Override
    public ListenableFuture<byte[]> decryptAsync(byte[] ciphertext, byte[] iv, byte[] authenticationData, byte[] authenticationTag, String algorithm) {

        if (implementation == null) {
            return null;
        }

        if (Strings.isNullOrWhiteSpace(algorithm)) {
            algorithm = getDefaultEncryptionAlgorithm();
        }

        // Never local
        ListenableFuture<ServiceResponse<KeyOperationResult>> futureCall =
                client.decryptAsync(
                        implementation.getKid(),
                        algorithm,
                        ciphertext,
                        null);
        return Futures.transform(futureCall, new DecryptResultTransform());
    }

    @Override
    public ListenableFuture<Triple<byte[], byte[], String>> encryptAsync(byte[] plaintext, byte[] iv, byte[] authenticationData, String algorithm) throws NoSuchAlgorithmException {
        if (implementation == null) {
            return null;
        }

        return implementation.encryptAsync(plaintext, iv, authenticationData, algorithm);
    }

    @Override
    public ListenableFuture<Pair<byte[], String>> wrapKeyAsync(byte[] plaintext, String algorithm) throws NoSuchAlgorithmException {
        if (implementation == null) {
            return null;
        }

        return implementation.wrapKeyAsync(plaintext, algorithm);
    }

    @Override
    public ListenableFuture<byte[]> unwrapKeyAsync(byte[] ciphertext, String algorithm) {
        if (implementation == null) {
            return null;
        }

        if (Strings.isNullOrWhiteSpace(algorithm)) {
            algorithm = getDefaultKeyWrapAlgorithm();
        }

        // Never local
        ListenableFuture<ServiceResponse<KeyOperationResult>> futureCall = 
                client.unwrapKeyAsync(
                        implementation.getKid(),
                        algorithm,
                        ciphertext,
                        null);
        return Futures.transform(futureCall, new DecryptResultTransform());
    }

    @Override
    public ListenableFuture<Pair<byte[], String>> signAsync(byte[] digest, String algorithm) throws NoSuchAlgorithmException {
        if (implementation == null) {
            return null;
        }

        if (Strings.isNullOrWhiteSpace(algorithm)) {
            algorithm = getDefaultSignatureAlgorithm();
        }
        
        // Never local
        ListenableFuture<ServiceResponse<KeyOperationResult>>  futureCall = 
                client.signAsync(
                        implementation.getKid(),
                        algorithm,
                        digest,
                        null);
        return Futures.transform(futureCall, new SignResultTransform(algorithm));
    }

    @Override
    public ListenableFuture<Boolean> verifyAsync(byte[] digest, byte[] signature, String algorithm) throws NoSuchAlgorithmException {
        if (implementation == null) {
            return null;
        }

        return implementation.verifyAsync(digest, signature, algorithm);
    }
}
