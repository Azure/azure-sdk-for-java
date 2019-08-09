// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

/**
 * Abstract base class for all symmetric encryption implementation.
 *
 */
abstract class SymmetricEncryptionAlgorithm extends LocalEncryptionAlgorithm {

    /*
     * Constructor.
     * 
     * @param name The name of the algorithm.
     */
    SymmetricEncryptionAlgorithm(String name) {
        super(name);
    }

    /*
     * Creates a {@link ICryptoTransform} implementation for encryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     *          The key material to be used.
     * @param iv
     *          The initialization vector to be used.
     * @param authenticationData
     *          The authentication data to be used with authenticating encryption implementation (ignored for non-authenticating implementation)
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createEncryptor(byte[] key, byte[] iv, byte[] authenticationData) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException;

    /*
     * Creates a {@link ICryptoTransform} implementation for encryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     *          The key material to be used.
     * @param iv
     *          The initialization vector to be used.
     * @param authenticationData
     *          The authentication data to be used with authenticating encryption implementation (ignored for non-authenticating implementation)
     * @param provider
     *          The provider to use.
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createEncryptor(byte[] key, byte[] iv, byte[] authenticationData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException;

    /*
     * Creates a {@link ICryptoTransform} implementation for decryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     *          The key material to be used.
     * @param iv
     *          The initialization vector to be used.
     * @param authenticationData
     *          The authentication data to be used with authenticating encryption implementation (ignored for non-authenticating implementation)
     * @param authenticationTag
     *          The authentication tag to verify when using authenticating encryption implementation (ignored for non-authenticating implementation)
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createDecryptor(byte[] key, byte[] iv, byte[] authenticationData, byte[] authenticationTag) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException;

    /*
     * Creates a {@link ICryptoTransform} implementation for decryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     *          The key material to be used.
     * @param iv
     *          The initialization vector to be used.
     * @param authenticationData
     *          The authentication data to be used with authenticating encryption implementation (ignored for non-authenticating implementation)
     * @param authenticationTag
     *          The authentication tag to verify when using authenticating encryption implementation (ignored for non-authenticating implementation)
     * @param provider
     *          The provider to use.
     * @return A {@link ICryptoTransform} implementation
     */
    abstract ICryptoTransform createDecryptor(byte[] key, byte[] iv, byte[] authenticationData, byte[] authenticationTag, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException;

}
