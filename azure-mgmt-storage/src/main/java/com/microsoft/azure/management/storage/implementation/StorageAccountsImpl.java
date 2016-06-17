/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.api.CheckNameAvailabilityResultInner;
import com.microsoft.azure.management.storage.implementation.api.SkuName;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountInner;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountsInner;
import com.microsoft.rest.RestException;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.List;

/**
 * The implementation of StorageAccounts and its parent interfaces.
 */
class StorageAccountsImpl
        extends GroupableResourcesImpl<StorageAccount, StorageAccountImpl, StorageAccountInner, StorageAccountsInner>
        implements StorageAccounts {
    private final PagedListConverter<StorageAccountInner, StorageAccount> converter;

    StorageAccountsImpl(final StorageAccountsInner client, final ResourceManager resourceManager) {
        super(resourceManager, client);
        this.converter = new PagedListConverter<StorageAccountInner, StorageAccount>() {
            @Override
            public StorageAccount typeConvert(StorageAccountInner storageAccountInner) {
                return createFluentModel(storageAccountInner);
            }
        };
    }

    @Override
    public CheckNameAvailabilityResult checkNameAvailability(String name) throws CloudException, IOException {
        CheckNameAvailabilityResultInner inner = this.innerCollection.checkNameAvailability(name).getBody();
        return new CheckNameAvailabilityResult(inner);
    }

    @Override
    public PagedList<StorageAccount> list() throws CloudException, IOException {
        ServiceResponse<List<StorageAccountInner>> response = this.innerCollection.list();
        return converter.convert(toPagedList(response.getBody()));
    }

    @Override
    public PagedList<StorageAccount> listByGroup(String groupName) throws CloudException, IOException {
        ServiceResponse<List<StorageAccountInner>> response = this.innerCollection.listByResourceGroup(groupName);
        return converter.convert(toPagedList(response.getBody()));
    }

    @Override
    public StorageAccount getByGroup(String groupName, String name) throws CloudException, IOException {
        ServiceResponse<StorageAccountInner> serviceResponse = this.innerCollection.getProperties(groupName, name);
        return createFluentModel(serviceResponse.getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        this.delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.innerCollection.delete(groupName, name);
    }

    @Override
    public StorageAccountImpl define(String name) {
        return createFluentModel(name)
                .withSku(SkuName.STANDARD_GRS)
                .withGeneralPurposeAccountKind();
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

    @Override
    protected StorageAccountImpl createFluentModel(String name) {
        return new StorageAccountImpl(name, new StorageAccountInner(), this.innerCollection, this.resourceManager);
    }

    @Override
    protected StorageAccountImpl createFluentModel(StorageAccountInner storageAccountInner) {
        return new StorageAccountImpl(storageAccountInner.name(), storageAccountInner, this.innerCollection, this.resourceManager);
    }
}
