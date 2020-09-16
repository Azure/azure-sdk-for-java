// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.cosmos.encryption;

public final class DecryptionResult {
    private final byte[] encryptedContent;
    private final Exception exception;

    private DecryptionResult(byte[] encryptedContent, Exception exception) {
        this.encryptedContent = encryptedContent;
        this.exception = exception;
    }

    /**
     * Returns the content failed to be decrypted.
     * @return the byte array content.
     */
    public byte[] getEncryptedContent() {
        return encryptedContent;
    }

    /**
     * The failure on decryption
     * @return exception.
     */
    public Exception getException() {
        return exception;
    }

    static DecryptionResult createFailure(byte[] encryptedContent, Exception exception) {
        return new DecryptionResult(encryptedContent, exception);
    }
}
