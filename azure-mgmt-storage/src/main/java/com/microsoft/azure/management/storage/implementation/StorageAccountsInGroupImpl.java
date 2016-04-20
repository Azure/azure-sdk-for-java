package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.StorageAccountsInGroup;
import com.microsoft.azure.management.storage.models.StorageAccount;
import java.io.IOException;
import java.util.List;


    // Implementation of storage account collection with a resource group context.
    //
public class StorageAccountsInGroupImpl implements StorageAccountsInGroup {
    private ResourceGroup resourceGroup;
    private StorageAccounts storageAccountsCore;

    public StorageAccountsInGroupImpl(StorageAccounts storageAccountsCore, ResourceGroup resourceGroup) {
        this.storageAccountsCore = storageAccountsCore;
        this.resourceGroup = resourceGroup;
    }

    public StorageAccount.DefinitionWithGroupContextBlank define(String name) {
        return storageAccountsCore.define(name, resourceGroup);
    }

    public void delete(String name) throws Exception {
        storageAccountsCore.delete(resourceGroup.name(), name);
    }

     public List<StorageAccount> list() throws CloudException, IOException {
        return storageAccountsCore.list(resourceGroup.name());
     }
}
