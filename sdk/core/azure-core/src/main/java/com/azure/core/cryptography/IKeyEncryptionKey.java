// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.cryptography;

import reactor.core.publisher.Mono;


/**
 * A key which is used to encrypt, or wrap, another key.
 */
public interface IKeyEncryptionKey {
    
    /**
     * Encrypts the specified key using the specified algorithm.
     *
     * @param algorithm The key wrap algorithm used to encrypt the specified key.
     * @param key The key content to be decrypted.
     * @return The decrypted key bytes.
     */
    Mono<byte[]> wrapKey(String algorithm, byte[] key);

    /**
     * Decrypts the specified encrypted key using the specified algorithm.
     *
     * @param algorithm The key wrap algorithm which was used to encrypt the specified encrypted key.
     * @param encryptedKey The encrypted key content to be decrypted.
     * @return The decrypted key bytes.
     */
    Mono<byte[]> unwrapKey(String algorithm, byte[] encryptedKey);
}
