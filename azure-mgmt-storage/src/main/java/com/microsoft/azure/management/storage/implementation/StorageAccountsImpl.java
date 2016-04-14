package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.management.storage.models.implementation.api.StorageAccountCreateParametersInner;
import com.microsoft.azure.management.storage.models.implementation.api.StorageAccountInner;
import com.microsoft.azure.management.storage.models.implementation.api.StorageAccountUpdateParametersInner;
import com.microsoft.rest.ServiceCallback;
import org.apache.commons.lang3.NotImplementedException;
import com.microsoft.azure.management.storage.models.implementation.StorageAccountImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StorageAccountsImpl
        implements StorageAccounts {
    private final StorageManagementClientImpl client;
    private final ResourceManagementClientImpl resourceClient;

    public StorageAccountsImpl(StorageManagementClientImpl client, ResourceManagementClientImpl resourceClient) {
        this.client = client;
        this.resourceClient = resourceClient;
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


    /***************************************************
     * Helpers
     ***************************************************/

    private StorageAccountImpl createWrapper(String id) {
        StorageAccountInner storageAccount = new StorageAccountInner();
        return new StorageAccountImpl(id, storageAccount, this.client, this.resourceClient);
    }
}
