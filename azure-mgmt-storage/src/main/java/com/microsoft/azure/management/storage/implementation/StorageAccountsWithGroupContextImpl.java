package com.microsoft.azure.management.storage.implementation;


import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.ResourceGroupContext;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.azure.management.storage.models.StorageAccount;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.util.List;

public class StorageAccountsWithGroupContextImpl implements ResourceGroupContext.StorageAccounts {
    public StorageAccountsWithGroupContextImpl(StorageManagementClientImpl client, ResourceGroup resourceGroup) {
    }

    public StorageAccount.DefinitionWithGroupContextBlank define(String name) {
        return null;
    }

    public void delete(String name) {
        throw new NotImplementedException("delete");
    }

    public List<StorageAccount> list() throws CloudException, IOException {
        throw new NotImplementedException("list");
    }
}
