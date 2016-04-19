package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.ResourceGroupContext;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.models.StorageAccount;
import java.io.IOException;
import java.util.List;

public class ResourceGroupContextImpl {
    public class StorageAccountsImpl implements ResourceGroupContext.StorageAccounts {
        private ResourceGroup resourceGroup;
        private StorageAccounts storageAccounts;

        public StorageAccountsImpl(StorageAccounts storageAccounts, ResourceGroup resourceGroup) {
            this.storageAccounts = storageAccounts;
            this.resourceGroup = resourceGroup;
        }

        public StorageAccount.DefinitionWithGroupContextBlank define(String name) {
            return storageAccounts.define(name, resourceGroup);
        }

        public void delete(String name) throws Exception {
            storageAccounts.delete(resourceGroup.name(), name);
        }

        public List<StorageAccount> list() throws CloudException, IOException {
            return storageAccounts.list(resourceGroup.name());
        }
    }
}
