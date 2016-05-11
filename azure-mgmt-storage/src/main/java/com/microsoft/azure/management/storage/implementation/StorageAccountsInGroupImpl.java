package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.StorageAccount;
import java.io.IOException;


// Implementation of storage account collection with a resource group context.
//
public class StorageAccountsInGroupImpl implements StorageAccounts.InGroup {
    private final StorageAccounts storageAccounts;
    private final ResourceGroup resourceGroup;

    public StorageAccountsInGroupImpl(final StorageAccounts storageAccounts, final ResourceGroup resourceGroup) {
        this.storageAccounts = storageAccounts;
        this.resourceGroup = resourceGroup;
    }

    public StorageAccount.DefinitionProvisionable define(String name) {
        return storageAccounts.define(name)
                .withRegion(resourceGroup.location())
                .withExistingGroup(resourceGroup.name());
    }

    public void delete(String name) throws Exception {
        storageAccounts.delete(resourceGroup.name(), name);
    }

     public PagedList<StorageAccount> list() throws CloudException, IOException {
        return storageAccounts.list(resourceGroup.name());
     }
}
