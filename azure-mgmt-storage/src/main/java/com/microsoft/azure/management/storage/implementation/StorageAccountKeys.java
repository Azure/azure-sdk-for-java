package com.microsoft.azure.management.storage.implementation;

public class StorageAccountKeys {
    private String primaryKey;
    private String secondaryKey;

    public StorageAccountKeys(String primaryKey, String secondaryKey) {
        this.primaryKey = primaryKey;
        this.secondaryKey = secondaryKey;
    }

    public String primaryKey() {
        return primaryKey;
    }

    public String secondryKey() {
        return secondaryKey;
    }
}
