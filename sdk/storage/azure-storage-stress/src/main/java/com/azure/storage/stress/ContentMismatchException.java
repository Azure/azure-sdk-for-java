package com.azure.storage.stress;

public class ContentMismatchException extends RuntimeException {
    public ContentMismatchException() {
        super("crc mismatch");
    }
}
