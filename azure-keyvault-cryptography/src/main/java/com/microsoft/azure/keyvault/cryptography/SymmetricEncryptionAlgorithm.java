/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.NoSuchPaddingException;

/**
 * Abstract base class for all symmetric encryption algorithms.
 *
 */
public abstract class SymmetricEncryptionAlgorithm extends EncryptionAlgorithm {

    /**
     * Constructor.
     * 
     * @param name The name of the algorithm.
     */
    protected SymmetricEncryptionAlgorithm(String name) {
        super(name);
    }

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for encryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     * 			The key material to be used.
     * @param iv
     * 			The initialization vector to be used.
     * @param authenticationData
     * 			The authentication data to be used with authenticating encryption algorithms (ignored for non-authenticating algorithms)
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, byte[] authenticationData) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for encryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     * 			The key material to be used.
     * @param iv
     * 			The initialization vector to be used.
     * @param authenticationData
     * 			The authentication data to be used with authenticating encryption algorithms (ignored for non-authenticating algorithms)
     * @param provider
     * 			The provider to use.
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, byte[] authenticationData, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for decryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     * 			The key material to be used.
     * @param iv
     * 			The initialization vector to be used.
     * @param authenticationData
     * 			The authentication data to be used with authenticating encryption algorithms (ignored for non-authenticating algorithms)
     * @param authenticationTag
     *          The authentication tag to verify when using authenticating encryption algorithms (ignored for non-authenticating algorithms)
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, byte[] authenticationData, byte[] authenticationTag) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException;

    /**
     * Creates a {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation for decryption
     * using the supplied initialization vector and the specific provider for the Java Security API.
     * @param key
     * 			The key material to be used.
     * @param iv
     * 			The initialization vector to be used.
     * @param authenticationData
     * 			The authentication data to be used with authenticating encryption algorithms (ignored for non-authenticating algorithms)
     * @param authenticationTag
     *          The authentication tag to verify when using authenticating encryption algorithms (ignored for non-authenticating algorithms)
     * @param provider
     * 			The provider to use.
     * @return A {@link com.microsoft.azure.keyvault.cryptography.ICryptoTransform} implementation
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public abstract ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, byte[] authenticationData, byte[] authenticationTag, Provider provider) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException;

}
