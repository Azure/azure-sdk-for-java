package com.azure.storage.file.datalake.stress.utils;

public class ContentMismatchException extends RuntimeException {
    public ContentMismatchException() {
        super("crc mismatch");
    }
}
