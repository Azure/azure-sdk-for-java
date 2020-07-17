// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.guava27.Strings;

class EncryptionExceptionFactory {

    static class InvalidArgumentException extends IllegalArgumentException {
        public InvalidArgumentException(String msg, String argName) {
            super(Strings.lenientFormat("argName: %s, details: %s", argName, msg));
        }
    }

    static RuntimeException invalidKeySize(String algorithmName, int actualKeylength, int expectedLength) {
        return new InvalidArgumentException(
            Strings.lenientFormat("Invalid key size for %s; actual: %s, expected: %s",
                algorithmName, actualKeylength, expectedLength), "dataEncryptionKey");
    }

    static RuntimeException invalidCipherTextSize(int actualSize, int minimumSize) {
        return new InvalidArgumentException(
            Strings.lenientFormat("Invalid cipher text size; actual: %s, minimum expected: %s.",
                actualSize, minimumSize), "cipherText");
    }

    static RuntimeException invalidAlgorithmVersion(byte actual, byte expected) {
        return new InvalidArgumentException(
            Strings.lenientFormat("Invalid encryption algorithm version; actual: %s, expected: %s.",
                Bytes.toHex(actual), Bytes.toHex(expected)), "cipherText");
    }

    static RuntimeException invalidAuthenticationTag() {
        return new InvalidArgumentException(
            "Invalid authentication tag in cipher text.",
            "cipherText");
    }
}
