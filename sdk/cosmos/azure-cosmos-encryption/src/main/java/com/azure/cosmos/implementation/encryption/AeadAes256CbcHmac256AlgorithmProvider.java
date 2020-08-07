// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.EncryptionType;

public class AeadAes256CbcHmac256AlgorithmProvider {
    public static void generateRandomBytes(byte[] randomBytes) {
        SecurityUtility.generateRandomBytes(randomBytes);
    }

    public static AeadAes256CbcHmac256Algorithm createAlgorithm(byte[] encryptionKey, EncryptionType encryptionType, byte algorithmVersion) {
        AeadAes256CbcHmac256EncryptionKey dataEncryptionKey = new AeadAes256CbcHmac256EncryptionKey(encryptionKey, AeadAes256CbcHmac256Algorithm.ALGORITHM_NAME);
        return new AeadAes256CbcHmac256Algorithm(dataEncryptionKey, encryptionType, algorithmVersion);
    }
}
