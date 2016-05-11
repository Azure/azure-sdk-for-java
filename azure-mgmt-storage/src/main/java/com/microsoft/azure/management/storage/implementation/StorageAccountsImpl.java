package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountsInner;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountInner;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.List;

public class StorageAccountsImpl
        implements StorageAccounts {
    private final StorageAccountsInner client;
    private final ResourceGroups resourceGroups;
    private final PagedListConverter<StorageAccountInner, StorageAccount> converter;

    public StorageAccountsImpl(final StorageAccountsInner client, final ResourceGroups resourceGroups) {
        this.client = client;
        this.resourceGroups = resourceGroups;
        this.converter = new PagedListConverter<StorageAccountInner, StorageAccount>() {
            @Override
            public StorageAccount typeConvert(StorageAccountInner storageAccountInner) {
                return createFluentModel(storageAccountInner);
            }
        };
    }

    @Override
    public PagedList<StorageAccount> list() throws CloudException, IOException {
        ServiceResponse<List<StorageAccountInner>> response = client.list();
        return converter.convert(toPagedList(response.getBody()));
    }

    @Override
    public PagedList<StorageAccount> list(String groupName) throws CloudException, IOException {
        ServiceResponse<List<StorageAccountInner>> response = client.listByResourceGroup(groupName);
        return converter.convert(toPagedList(response.getBody()));
    }

    @Override
    public StorageAccount get(String name) throws CloudException, IOException {
        ServiceResponse<List<StorageAccountInner>> response = client.list();
        for (StorageAccountInner storageAccountInner : response.getBody()) {
            if (storageAccountInner.name() == name) {
                return createFluentModel(storageAccountInner);
            }
        }
        return null;
    }

    @Override
    public StorageAccount get(String groupName, String name) throws CloudException, IOException {
        ServiceResponse<StorageAccountInner> serviceResponse = this.client.getProperties(groupName, name);
        return createFluentModel(serviceResponse.getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        this.delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.client.delete(groupName, name);
    }

    @Override
    public StorageAccount.DefinitionBlank define(String name) {
        return createFluentModel(name);
    }

    private PagedList<StorageAccountInner> toPagedList(List<StorageAccountInner> list) {
        PageImpl<StorageAccountInner> page = new PageImpl<>();
        page.setItems(list);
        page.setNextPageLink(null);
        return new PagedList<StorageAccountInner>(page) {
            @Override
            public Page<StorageAccountInner> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };
    }

    /** Fluent model create helpers **/

    private StorageAccountImpl createFluentModel(String name) {
        StorageAccountInner storageAccountInner = new StorageAccountInner();
        return new StorageAccountImpl(name, storageAccountInner, this.client, this.resourceGroups);
    }

    private StorageAccountImpl createFluentModel(StorageAccountInner storageAccountInner) {
        return new StorageAccountImpl(storageAccountInner.name(), storageAccountInner, this.client, this.resourceGroups);
    }
}
