package com.azure.storage.blob.stress.utils;

public class ContentMismatchException extends RuntimeException {
    public ContentMismatchException() {
        super("crc mismatch");
    }
}
