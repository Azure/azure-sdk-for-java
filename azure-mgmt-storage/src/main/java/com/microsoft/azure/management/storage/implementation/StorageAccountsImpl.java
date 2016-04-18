package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.management.storage.models.implementation.api.StorageAccountInner;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.azure.management.storage.models.implementation.StorageAccountImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StorageAccountsImpl
        implements StorageAccounts {
    private final StorageManagementClientImpl client;
    private final ResourceGroups resourceGroups;

    public StorageAccountsImpl(StorageManagementClientImpl client, ResourceGroups resourceGroups) {
        this.client = client;
        this.resourceGroups = resourceGroups;
    }

    @Override
    public List<StorageAccount> list() throws CloudException, IOException {
        ServiceResponse<List<StorageAccountInner>> list = client.storageAccounts().list();
        return createFluentWrapperList(list.getBody());
    }

    public List<StorageAccount> list(String groupName) throws Exception {
        ServiceResponse<List<StorageAccountInner>> list = client.storageAccounts().listByResourceGroup(groupName);
        return createFluentWrapperList(list.getBody());
    }

    public StorageAccount get(String groupName, String name) throws Exception {
        ServiceResponse<StorageAccountInner> serviceResponse = this.client.storageAccounts().getProperties(groupName, name);
        return createFluentWrapper(serviceResponse.getBody());
    }

    public void delete(String id) throws Exception {
        this.client.storageAccounts().delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    public void delete(String groupName, String name) throws Exception {
        this.client.storageAccounts().delete(groupName, name);
    }

    public StorageAccount.DefinitionBlank define(String name) throws Exception {
        return createFluentWrapper(name);
    }

    /***************************************************
     * Helpers
     ***************************************************/

    private StorageAccountImpl createFluentWrapper(String name) {
        StorageAccountInner storageAccountInner = new StorageAccountInner();
        return new StorageAccountImpl(name, storageAccountInner, this.client, this.resourceGroups);
    }

    private StorageAccountImpl createFluentWrapper(StorageAccountInner storageAccountInner) {
        return new StorageAccountImpl(storageAccountInner.name(), storageAccountInner, this.client, this.resourceGroups);
    }

    private List<StorageAccount> createFluentWrapperList(List<StorageAccountInner> list) {
        List<StorageAccount> result = new ArrayList<>();
        for (StorageAccountInner inner : list) {
            result.add(createFluentWrapper(inner));
        }
        return result;
    }
}
