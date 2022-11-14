// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * Common interface wrapper for MessageDigest.
 */
public final class MessageDigestChecksum implements Checksum {
    private final MessageDigest digest;

    public static MessageDigestChecksum create(MessageDigest digest) {
        return new MessageDigestChecksum(digest);
    }

    private MessageDigestChecksum(MessageDigest digest) {
        this.digest = digest;
    }

    @Override
    public void update(byte[] b, int off, int len) {
        digest.update(b, off, len);
    }

    @Override
    public void update(ByteBuffer buffer) {
        digest.update(buffer);
    }

    public byte[] getValue() {
        return digest.digest();
    }

    @Override
    public void reset() {
        digest.reset();
    }
}
