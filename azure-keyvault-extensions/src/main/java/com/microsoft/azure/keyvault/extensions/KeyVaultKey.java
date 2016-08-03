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
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.cryptography.RsaKey;
import com.microsoft.azure.keyvault.cryptography.Strings;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.models.JsonWebKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

public class KeyVaultKey implements IKey {

    class FutureDecryptResult extends FutureAdapter<KeyOperationResult, byte[]> {

        FutureDecryptResult() {
            super();
        }

        @Override
        protected byte[] translate(KeyOperationResult result) throws IOException {
            return result.result();
        }
    }

    class FutureSignResult extends FutureAdapter<KeyOperationResult, Pair<byte[], String>> {

        private final String _algorithm;

        FutureSignResult(String algorithm) {
            super();
            _algorithm = algorithm;
        }

        @Override
        protected Pair<byte[], String> translate(KeyOperationResult result) throws IOException {
            return Pair.of(result.result(), _algorithm);
        }
    }

    private final KeyVaultClient _client;
    private IKey                 _implementation;

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
            _implementation = new RsaKey(key.kid(), key.toRSA(false));
        } else if (key.kty().equals(JsonWebKeyType.RSAHSM)) {
        	// The private key is not available for KeyVault keys
            _implementation = new RsaKey(key.kid(), key.toRSA(false));
        }

        if (_implementation == null) {
            throw new IllegalArgumentException(String.format("The key type %s is not supported", key.kty()));
        }

        _client = client;
    }

    @Override
    public void close() throws IOException {
        if (_implementation != null) {
            _implementation.close();
        }
    }

    @Override
    public String getDefaultEncryptionAlgorithm() {
        if (_implementation == null) {
            return null;
        }

        return _implementation.getDefaultEncryptionAlgorithm();
    }

    @Override
    public String getDefaultKeyWrapAlgorithm() {

        if (_implementation == null) {
            return null;
        }

        return _implementation.getDefaultKeyWrapAlgorithm();
    }

    @Override
    public String getDefaultSignatureAlgorithm() {

        if (_implementation == null) {
            return null;
        }

        return _implementation.getDefaultSignatureAlgorithm();
    }

    @Override
    public String getKid() {

        if (_implementation == null) {
            return null;
        }

        return _implementation.getKid();
    }

    @Override
    public Future<byte[]> decryptAsync(byte[] ciphertext, byte[] iv, byte[] authenticationData, byte[] authenticationTag, String algorithm) {

    	if (_implementation == null) {
            return null;
        }

        if (Strings.isNullOrWhiteSpace(algorithm)) {
            algorithm = getDefaultEncryptionAlgorithm();
        }

        // Never local
        FutureDecryptResult result = new FutureDecryptResult();
        
        result.setServiceCall(
        	_client.decrypt(
        		_implementation.getKid(),
        		algorithm,
        		ciphertext,
        		result ) );
        
        return result;
    }

    @Override
    public Future<Triple<byte[], byte[], String>> encryptAsync(byte[] plaintext, byte[] iv, byte[] authenticationData, String algorithm) throws NoSuchAlgorithmException {
        if (_implementation == null) {
            return null;
        }

        return _implementation.encryptAsync(plaintext, iv, authenticationData, algorithm);
    }

    @Override
    public Future<Pair<byte[], String>> wrapKeyAsync(byte[] plaintext, String algorithm) throws NoSuchAlgorithmException {
        if (_implementation == null) {
            return null;
        }

        return _implementation.wrapKeyAsync(plaintext, algorithm);
    }

    @Override
    public Future<byte[]> unwrapKeyAsync(byte[] ciphertext, String algorithm) {
        if (_implementation == null) {
            return null;
        }

        if (Strings.isNullOrWhiteSpace(algorithm)) {
            algorithm = getDefaultKeyWrapAlgorithm();
        }

        // Never local
        FutureDecryptResult result = new FutureDecryptResult();
        
        result.setServiceCall(
        	_client.unwrapKey(
        		_implementation.getKid(),
        		algorithm,
        		ciphertext,
        		result ) );
        
        return result;

    }

    @Override
    public Future<Pair<byte[], String>> signAsync(byte[] digest, String algorithm) {
        if (_implementation == null) {
            return null;
        }

        if (Strings.isNullOrWhiteSpace(algorithm)) {
            algorithm = getDefaultSignatureAlgorithm();
        }
        
        // Never local
        FutureSignResult result = new FutureSignResult(algorithm);
        
        result.setServiceCall(
        	_client.sign(
        		_implementation.getKid(),
        		algorithm,
        		digest,
        		result ) );
        
        return result;

    }

    @Override
    public Future<Boolean> verifyAsync(byte[] digest, byte[] signature, String algorithm) {
        if (_implementation == null) {
            return null;
        }

        return _implementation.verifyAsync(digest, signature, algorithm);
    }
}
