package com.azure.storage.blob;

public class UnexpectedLengthException extends IllegalStateException {
    private final long bytesRead;
    private final long bytesExpected;

    public UnexpectedLengthException(String message, long bytesRead, long bytesExpected) {
        super(message);
        this.bytesRead = bytesRead;
        this.bytesExpected = bytesExpected;
    }

    public long bytesRead() {
        return this.bytesRead;
    }

    public long bytesExpected() {
        return this.bytesExpected;
    }
}
