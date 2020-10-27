// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

// TODO: once public api is finalize clean up
public abstract class EncryptionKeyStoreProvider {
    public String providerName;

    public enum KeyEncryptionKeyAlgorithm {
        /// <summary>
        /// RSA public key cryptography algorithm with Optimal Asymmetric Encryption Padding (OAEP) padding.
        /// </summary>
        RSA_OAEP;

        // TODO: once encryption algo finalized update here
        static KeyEncryptionKeyAlgorithm fromString(String val) {
            return RSA_OAEP;
        }
    }

    /// <summary>
    /// Unwraps the specified <paramref name="encryptedKey"/> of a data encryption key. The encrypted value is
    // expected to be encrypted using
    /// the key encryption key with the specified <paramref name="encryptionKeyId"/> and using the specified
    // <paramref name="algorithm"/>.
    /// </summary>
    /// <param name="encryptionKeyId">The key Id tells the provider where to find the key.</param>
    /// <param name="algorithm">The encryption algorithm.</param>
    /// <param name="encryptedKey">The ciphertext key.</param>
    /// <returns>The unwrapped data encryption key.</returns>
    public abstract byte[] unwrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm, byte[] encryptedKey);

    /// <summary>
    /// Wraps a data encryption key using the key encryption key with the specified <paramref
    // name="encryptionKeyId"/> and using the specified <paramref name="algorithm"/>.
    /// </summary>
    /// <param name="encryptionKeyId">The key Id tells the provider where to find the key.</param>
    /// <param name="algorithm">The encryption algorithm.</param>
    /// <param name="key">The plaintext key</param>
    /// <returns>The wrapped data encryption key.</returns>
    public abstract byte[] wrapKey(String encryptionKeyId, KeyEncryptionKeyAlgorithm algorithm, byte[] key);
}
