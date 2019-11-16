// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Defines the basic operations of cryptographic transformations.
 */
interface ICryptoTransform {

    /**
     * Transforms the specified region of the specified byte array as a single operation.
     *
     * @param input The byte array to be transformed
     * @return The transformed result.
     */
    byte[] doFinal(byte[] input)
        throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException;


    /**
     * Factory Strategy for <code>ICryptoTransform</code> instances
     *
     * @param <T> Type of the context object used for a particular factory implementation.
     */
    interface Factory<T> {

        /**
         * @param context The context required for the creation of a new ICryptoTransform instance
         * @return The new instance.
         * @throws InvalidKeyException if a particular key is invalid (invalid encoding, wrong length, uninitialized, etc).
         * @throws NoSuchAlgorithmException if a particular cryptographic algorithm is requested but is not available in the environment.
         * @throws NoSuchPaddingException if a particular padding mechanism is requested but is not available in the environment.
         * @throws InvalidAlgorithmParameterException if a particular algorithm parameters is invalid or inappropriate.
         */
        ICryptoTransform create(T context) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException;
    }
}
