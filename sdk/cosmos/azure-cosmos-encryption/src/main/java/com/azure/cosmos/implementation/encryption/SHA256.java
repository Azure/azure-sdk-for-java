// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import java.io.Closeable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class SHA256 implements Closeable {
    private final MessageDigest digest;

    private SHA256() {
        digest = getMessageDigest();
    }

    public static SHA256 create() {
        return new SHA256();
    }

    public static MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {

    }

    public byte[] computeHash(byte[] input) {
        return digest.digest(input);
    }
}
