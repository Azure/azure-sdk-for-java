package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;

public class UsagesImpl
        implements Usages {
    private final StorageManagementClientImpl client;

    public UsagesImpl(StorageManagementClientImpl client) {
        this.client = client;
    }
}
