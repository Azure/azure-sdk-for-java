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

package com.microsoft.azure.keyvault.core;

import java.io.Closeable;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.util.concurrent.ListenableFuture;


/**
 * Interface for representing cryptographic keys with the Microsoft Azure Key
 * Vault libraries.
 */
public interface IKey extends Closeable {

    /**
     * The default encryption algorithm for this key, using the representations
     * from Json Web Key Algorithms, RFC7513.
     *
     * @return The default encryption algorithm for this key.
     */
    String getDefaultEncryptionAlgorithm();

    /**
     * The default key wrap algorithm for this key, using the representations
     * from Json Web Key Algorithms, RFC7513.
     *
     * @return The default key wrap algorithm for this key.
     */
    String getDefaultKeyWrapAlgorithm();

    /**
     * The default signature algorithm for this key, using the representations
     * from Json Web Key Algorithms, RFC7513.
     *
     * @return The default signature algorithm for this key.
     */
    String getDefaultSignatureAlgorithm();

    /**
     * The unique key identifier for this key.
     *
     * @return The key identifier
     */
    String getKid();

    /**
     * Decrypts the specified cipher text. Note that not all algorithms require,
     * or support, all parameters.
     *
     * @param ciphertext
     *            The cipher text to decrypt
     * @param iv
     *            The initialization vector (optional with some algorithms)
     * @param authenticationData
     *            Additional authentication data (optional with some algorithms)
     * @param authenticationTag
     *            The authentication tag from the encrypt operation (optional
     *            with some algorithms)
     * @param algorithm
     *            The encryption algorithm to use, must be supplied
     * @return A ListenableFuture containing the plain text
     * @throws NoSuchAlgorithmException the algorithm is not valid
     */
    ListenableFuture<byte[]> decryptAsync(final byte[] ciphertext, final byte[] iv, final byte[] authenticationData, final byte[] authenticationTag, final String algorithm) throws NoSuchAlgorithmException;

    /**
     * Encrypts the specified plain text. Note that not all algorithms require,
     * or support, all parameters.
     *
     * @param plaintext
     *            The plain text to encrypt
     * @param iv
     *            The initialization vector (optional with some algorithms)
     * @param authenticationData
     *            Additional authentication data (optional with some algorithms)
     * @param algorithm
     *            The encryption algorithm to use, defaults to the keys
     *            DefaultEncryptionAlgorithm
     * @return A ListenableFuture containing the cipher text, the authentication
     *         tag and the algorithm that was used
     * @throws NoSuchAlgorithmException the algorithm is not valid
     */
    ListenableFuture<Triple<byte[], byte[], String>> encryptAsync(final byte[] plaintext, final byte[] iv, final byte[] authenticationData, final String algorithm) throws NoSuchAlgorithmException;

    /**
     * Wraps (encrypts) the specified symmetric key material using the specified
     * algorithm, or the keys DefaultKeyWrapAlgorithm if none is specified.
     *
     * @param key
     *            The symmetric key to wrap
     * @param algorithm
     *            The wrapping algorithm to use, defaults to the keys
     *            DefaultKeyWrapAlgorithm
     * @return ListenableFuture containing the encrypted key and the algorithm
     *         that was used
     * @throws NoSuchAlgorithmException the algorithm is not valid
     */
    ListenableFuture<Pair<byte[], String>> wrapKeyAsync(final byte[] key, final String algorithm) throws NoSuchAlgorithmException;

    /**
     * Unwraps (decrypts) the specified encryped key material.
     *
     * @param encryptedKey
     *            The encrypted key to decrypt
     * @param algorithm
     *            The algorithm to use, must be supplied
     * @return A ListenableFuture containing the unwrapped key
     * @throws NoSuchAlgorithmException the algorithm is not valid
     */
    ListenableFuture<byte[]> unwrapKeyAsync(final byte[] encryptedKey, final String algorithm) throws NoSuchAlgorithmException;

    /**
     * Signs the specified digest using the specified algorithm, or the keys
     * DefaultSignatureAlgorithm if no algorithm is specified.
     *
     * @param digest
     *            The digest to sign
     * @param algorithm
     *            The signature algorithm to use
     * @return A ListenableFuture containing the signature and the algorithm used.
     * @throws NoSuchAlgorithmException the algorithm is not valid
     */
    ListenableFuture<Pair<byte[], String>> signAsync(final byte[] digest, final String algorithm) throws NoSuchAlgorithmException;

    /**
     * Verifies the supplied signature value using the supplied digest and
     * algorithm.
     *
     * @param digest
     *            The digest input
     * @param signature
     *            The signature to verify
     * @param algorithm
     *            The algorithm to use, must be provided
     * @return A ListenableFuture containing a boolean result
     * @throws NoSuchAlgorithmException the algorithm is not valid
     */
    ListenableFuture<Boolean> verifyAsync(final byte[] digest, final byte[] signature, final String algorithm) throws NoSuchAlgorithmException;
}
