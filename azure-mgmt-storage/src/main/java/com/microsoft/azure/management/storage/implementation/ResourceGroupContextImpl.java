package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.ResourceGroupContext;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.models.StorageAccount;
import java.io.IOException;
import java.util.List;

public class ResourceGroupContextImpl {
    // The resource group in which child collection types [e.g. ResourceGroupContextImpl.StorageAccountsImpl ] belongs to.
    private ResourceGroup resourceGroup;

    /**
     * Initializes an instance of ResourceGroupContextImpl.
     *
     * @param resourceGroup the ResourceGroup in the context.
     */
    public ResourceGroupContextImpl(ResourceGroup resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The Storage account collection in resource group context.
     *
     * @param storageAccountsCore the storage account collection in subscription context.
     */
    public ResourceGroupContext.StorageAccounts storageAccounts(StorageAccounts storageAccountsCore) {
        return new StorageAccountsImpl(storageAccountsCore, resourceGroup);
    }

    // Implementation of storage account collection with a resource group context.
    //
    class StorageAccountsImpl implements ResourceGroupContext.StorageAccounts {
        private ResourceGroup resourceGroup;
        private StorageAccounts storageAccountsCore;

        public StorageAccountsImpl(StorageAccounts storageAccountsCore, ResourceGroup resourceGroup) {
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
}
