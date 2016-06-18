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
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.implementation.api.SkuName;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountInner;
import com.microsoft.azure.management.storage.implementation.api.StorageAccountsInner;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.List;

/**
 * The implementation of StorageAccounts and its parent interfaces.
 */
class StorageAccountsImpl
        extends GroupableResourcesImpl<StorageAccount, StorageAccountImpl, StorageAccountInner, StorageAccountsInner>
        implements StorageAccounts {

    StorageAccountsImpl(final StorageAccountsInner client, final ResourceManager resourceManager) {
        super(resourceManager, client);
    }

    @Override
    public CheckNameAvailabilityResult checkNameAvailability(String name) throws CloudException, IOException {
        return new CheckNameAvailabilityResult(this.innerCollection.checkNameAvailability(name).getBody());
    }

    @Override
    public PagedList<StorageAccount> list() throws CloudException, IOException {
        return this.converter.convert(toPagedList(this.innerCollection.list().getBody()));
    }

    @Override
    public PagedList<StorageAccount> listByGroup(String groupName) throws CloudException, IOException {
        return this.converter.convert(toPagedList(this.innerCollection.listByResourceGroup(groupName).getBody()));
    }

    @Override
    public StorageAccount getByGroup(String groupName, String name) throws CloudException, IOException {
        return createFluentModel(this.innerCollection.getProperties(groupName, name).getBody());
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
        return new StorageAccountImpl(
                name,
                new StorageAccountInner(),
                this.innerCollection,
                this.resourceManager);
    }

    @Override
    protected StorageAccountImpl createFluentModel(StorageAccountInner storageAccountInner) {
        return new StorageAccountImpl(
                storageAccountInner.name(),
                storageAccountInner,
                this.innerCollection,
                this.resourceManager);
    }
}
