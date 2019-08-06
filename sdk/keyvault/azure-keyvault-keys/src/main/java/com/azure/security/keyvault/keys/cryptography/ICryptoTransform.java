// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Defines the basic operations of cryptographic transformations.
 *
 */
interface ICryptoTransform {

    /**
     * Transforms the specified region of the specified byte array as a single operation.
     * 
     * @param input The byte array to be transformed
     * @return The transformed result.
     */
    byte[] doFinal(byte[] input) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException;
}
