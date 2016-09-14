/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * Defines the basic operations of cryptographic transformations.
 *
 */
public interface ICryptoTransform {

    /**
     * Transforms the specified region of the specified byte array as a single operation.
     * 
     * @param input
     * 			The byte array to be transformed
     * @return
     * 			The transformed result.
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public byte[] doFinal(byte[] input) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException;
}
