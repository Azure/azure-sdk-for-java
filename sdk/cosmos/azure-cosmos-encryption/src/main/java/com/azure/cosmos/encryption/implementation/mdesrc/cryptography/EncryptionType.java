/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * The type of encryption. The three encryption types are Plaintext, Deterministic, and Randomized. Plaintext unencrypted
 * data. Deterministic encryption always generates the same encrypted value for any given plain text value. Randomized
 * encryption uses a method that encrypts data in a less predictable manner. Randomized encryption is more secure.
 */
public enum EncryptionType {
    /**
     * Plaintext unencrypted data.
     */
    Plaintext,
    /**
     * Deterministic encryption always generates the same encrypted value for any given plain text value.
     */
    Deterministic,
    /**
     * Randomized encryption uses a method that encrypts data in a less predictable manner. Randomized encryption is
     * more secure.
     */
    Randomized
}
