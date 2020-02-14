package com.azure.storage.blob.quickquery.models;

public class BlobQuickQuerySerialization <T extends BlobQuickQuerySerialization<T>> {

    private char recordSeparator;

    public char getRecordSeparator() {
        return recordSeparator;
    }

    public T setRecordSeparator(char recordSeparator) {
        this.recordSeparator = recordSeparator;
        return (T) this;
    }

}
