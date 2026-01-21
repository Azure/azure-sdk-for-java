// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

class EncryptionExceptionFactory {

    static class InvalidArgumentException extends IllegalArgumentException {
        public InvalidArgumentException(String msg, String argName) {
            super(String.format("argName: %s, details: %s", argName, msg));
        }
    }

    static IllegalArgumentException invalidKeySize(String algorithmName, int actualKeylength, int expectedLength) {
        return new InvalidArgumentException(
            String.format("Invalid key size for %s; actual: %s, expected: %s",
                algorithmName, actualKeylength, expectedLength), "dataEncryptionKey");
    }

    static IllegalArgumentException invalidCipherTextSize(int actualSize, int minimumSize) {
        return new InvalidArgumentException(
            String.format("Invalid cipher text size; actual: %s, minimum expected: %s.",
                actualSize, minimumSize), "cipherText");
    }

    static RuntimeException invalidAuthenticationTag() {
        return new InvalidArgumentException(
            "Invalid authentication tag in cipher text.",
            "cipherText");
    }

    static Exception exceptionKeyNotFoundException(
        String message,
        Exception innerException) {
        return new IllegalArgumentException(message, innerException);
    }
}
