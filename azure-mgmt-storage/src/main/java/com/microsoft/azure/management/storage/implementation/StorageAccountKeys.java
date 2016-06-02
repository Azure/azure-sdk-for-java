package com.microsoft.azure.management.storage.implementation;

/**
 * An instance of this class contains information about the access keys of a storage account.
 */
public class StorageAccountKeys {
    private String primaryKey;
    private String secondaryKey;

    /**
     * Creates an instance of StorageAccountKeys with a primary key and a secondary key.
     * @param primaryKey the primary access key
     * @param secondaryKey the secondary access key
     */
    public StorageAccountKeys(String primaryKey, String secondaryKey) {
        this.primaryKey = primaryKey;
        this.secondaryKey = secondaryKey;
    }

    /**
     * @return the primary access key.
     */
    public String primaryKey() {
        return primaryKey;
    }

    /**
     * @return the secondary access key.
     */
    public String secondary() {
        return secondaryKey;
    }
}
