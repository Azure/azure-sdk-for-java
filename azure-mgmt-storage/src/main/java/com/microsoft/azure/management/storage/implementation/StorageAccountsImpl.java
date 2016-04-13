package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.rest.ServiceCallback;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StorageAccountsImpl
        implements StorageAccounts {
    private final StorageManagementClientImpl client;

    public StorageAccountsImpl(StorageManagementClientImpl client) {
        this.client = client;
    }

    public Map<String, StorageAccount> asMap() throws Exception {
        throw new NotImplementedException("asMap");
    }

    public Map<String, StorageAccount> asMap(String groupName) throws Exception {
        throw new NotImplementedException("asMap");
    }

    public List<StorageAccount> list(String groupName) throws Exception {
        throw new NotImplementedException("list");
    }

    public StorageAccount get(String groupName, String name) throws Exception {
        throw new NotImplementedException("get");
    }

    public StorageAccount.DefinitionBlank define(String name) throws Exception {
        throw new NotImplementedException("define");
    }

    public void delete(String id) throws Exception {
        throw new NotImplementedException("delete");
    }

    public void deleteAsync(String id, ServiceCallback<Void> callback) throws Exception {
        throw new NotImplementedException("deleteAsync");
    }

    public void delete(String groupName, String name) throws Exception {
        throw new NotImplementedException("delete");
    }

    @Override
    public List<StorageAccount> list() throws CloudException, IOException {
        return null;
    }
}
