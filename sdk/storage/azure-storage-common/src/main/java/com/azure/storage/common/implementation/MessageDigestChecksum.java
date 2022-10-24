package com.azure.storage.common.implementation;

import java.security.MessageDigest;

/**
 * Common interface wrapper for MessageDigest.
 */
public class MessageDigestChecksum implements Checksum {
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


    public byte[] getValue() {
        return digest.digest();
    }

    @Override
    public void reset() {
        digest.reset();
    }
}
