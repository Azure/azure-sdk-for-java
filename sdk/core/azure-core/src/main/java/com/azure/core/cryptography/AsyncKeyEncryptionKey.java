// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.cryptography;

import reactor.core.publisher.Mono;

/**
 * The AsyncKeyEncryptionKey defines asynchronous methods for encrypting and decrypting keys, also
 * known as key wrapping and unwrapping. It also supports signing and verifying data using the configured key.
 */
public interface AsyncKeyEncryptionKey {

    /**
     * Retrieves the key identifier.
     *
     * @return A {@link Mono} containing key identifier.
     */
    Mono<String> getKeyId();

    /**
     * Encrypts the specified key using the specified algorithm.
     *
     * @param algorithm The key wrap algorithm used to encrypt the specified key.
     * @param key The key content to be encrypted.
     * @return A {@link Mono} containing the encrypted key bytes.
     */
    Mono<byte[]> wrapKey(String algorithm, byte[] key);

    /**
     * Decrypts the specified encrypted key using the specified algorithm.
     *
     * @param algorithm The key wrap algorithm which was used to encrypt the specified encrypted key.
     * @param encryptedKey The encrypted key content to be decrypted.
     * @return A {@link Mono} containing the decrypted key bytes.
     */
    Mono<byte[]> unwrapKey(String algorithm, byte[] encryptedKey);
}
