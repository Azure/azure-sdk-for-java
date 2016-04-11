package com.microsoft.azure.management.storage;

import com.microsoft.azure.CloudException;

import java.io.IOException;

public interface AzureStorageAuthenticated {
    StorageAccounts storageAccounts() throws IOException, CloudException;
}
